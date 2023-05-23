package com.ljq.prepareLessons;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ljq.prepareLessons.utils.MD5Util;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Word;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author 李佳琪
 * 2023-04-25
 */
public class PrepareLessons {
    private static final String OPTION_1 = "\\(1\\)";
    private static final String OPTION_2 = "\\(2\\)";
    private static final String OPTION_3 = "\\(3\\)";
    private static final String OPTION_4 = "\\(4\\)";
    private static final String mergeFormat1 = "(1) %s {%s} (2) %s {%s}";
    private static final String mergeFormat2 = "(3) %s {%s} (4) %s {%s} %s";
    private static final String answerRex = "\\(([^()]+)\\)[^(]*$";
    private static int width;
    private static int height;
    private String env;
    private String fontName;

    private String inputImg = "";
    private String outputImg = "";
    private static OkHttpClient client;

    private static PrepareLessons instance;

    public static PrepareLessons getInstance(String inputImg, String outputImg, String env, String fontName) {
        if (instance == null) {
            instance = new PrepareLessons();
        }
        if (client == null) {
            System.out.println("---------client---------");
            client = new OkHttpClient();
        }
        return instance.init(inputImg, outputImg, env, fontName);
    }

    private PrepareLessons() {
    }

    private PrepareLessons init(String inputImg, String outputImg, String env, String fontName) {
        this.inputImg = inputImg;
        this.outputImg = outputImg;
        this.env = env;
        this.fontName = fontName;
        return this;
    }

    public String getInputImg() {
        return inputImg;
    }

    public void setInputImg(String inputImg) {
        this.inputImg = inputImg;
    }

    public String getOutputImg() {
        return outputImg;
    }

    public void setOutputImg(String outputImg) {
        this.outputImg = outputImg;
    }

/*    public static void main(String[] args) throws Exception {
        PrepareLessons pl = new PrepareLessons("C:\\Users\\Administrator\\Desktop\\AAA.jpg", "C:\\Users\\Administrator\\Desktop\\DONE-AAA2.jpg", "dev");
        pl.doPrepareLesson();
    }*/

    public void doPrepareLesson() throws Exception {
        List<Word> words = loadImg();
        //获取单词
        System.out.println("=============================================================================");
        StringBuilder sb = new StringBuilder();
        for (Word wd : words) {
            String text = wd.getText().trim();
            if (text.contains("(1)") && text.contains("(2)")) {
                sb.append(reEnAndZh(text, mergeFormat1, 1));
            } else if (text.contains("(3)") && text.contains("(4)")) {
                sb.append(reEnAndZh(text, mergeFormat2, 2));
                sb.append("\n");
            } else {
                //题干或其他数据
                sb.append(text.replaceAll(" — ", " _______ ")
                        .replaceAll(" __ ", " _______ ")
                        .replaceAll(" _._ ", " _______ "));
            }
            sb.append("\n");
        }

       // System.out.println(sb.toString());
        //生成图片
        generaImg(sb.toString());
        System.out.println("清除client");
        client.dispatcher().executorService().shutdown();
        client.connectionPool().evictAll();
    }

    private void generaImg(String content) throws IOException {
        Color backgroundColor = Color.WHITE;
        Color textColor = Color.BLACK;
        int fontSize = 26;
        Font font = new Font(fontName, Font.PLAIN, fontSize);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(backgroundColor);
        g.fillRect(0, 0, width, height);
        g.setColor(textColor);
        g.setFont(font);

        String[] lines = content.split("\n");
        int lineHeight = (int) (font.getSize() * 1.2);
        int y = 50;
        for (String line : lines) {
            if (line.contains("{") && line.contains("}")) {
                Pattern pattern = Pattern.compile("\\{([^}]*)\\}");
                Matcher matcher = pattern.matcher(line);
                AttributedString attributedString = new AttributedString(line);
                attributedString.addAttribute(TextAttribute.FONT, g.getFont());
                FontMetrics fontMetrics = g.getFontMetrics();

                //这行宽度
                int lineWidth = fontMetrics.stringWidth(line);
                if (lineWidth > width) {
                    int start = 0;
                    int end = 0;

                    while (end < line.length()) {
                        int nextSpace = 0;
                        if (line.contains("(2)")) {
                            nextSpace = line.indexOf("(2)", end + 1);
                        } else {
                            nextSpace = line.indexOf("(4)", end + 1);
                        }
                        if (nextSpace == -1) {
                            nextSpace = line.length();
                        }
                        String subLine = line.substring(start, nextSpace);
                        int subLineWidth = fontMetrics.stringWidth(subLine);
                        if (subLineWidth > width) {
                            subLine = line.substring(start, end);
                            AttributedString attributedSubString = new AttributedString(subLine);
                            attributedSubString.addAttribute(TextAttribute.FONT, g.getFont());
                            Matcher m2 = pattern.matcher(subLine);
                            if (m2.find()) {
                                int groupCount = m2.groupCount();
                                for (int i = 1; i <= groupCount; i++) {
                                    int S = m2.start();
                                    int E = m2.end();
                                    attributedSubString.addAttribute(TextAttribute.FOREGROUND, Color.RED, S, E);
                                }
                                g.drawString(attributedSubString.getIterator(), 10, y);
                            }

                            y += lineHeight;
                            start = end;
                        }

                        end = nextSpace;

                        if (end == line.length()) {
                            subLine = line.substring(start, end);
                            AttributedString attributedSubString = new AttributedString(subLine);
                            attributedSubString.addAttribute(TextAttribute.FONT, g.getFont());
                            Matcher m2 = pattern.matcher(subLine);
                            if (m2.find()) {
                                int groupCount = m2.groupCount();
                                for (int i = 1; i <= groupCount; i++) {
                                    int S = m2.start();
                                    int E = m2.end();
                                    attributedSubString.addAttribute(TextAttribute.FOREGROUND, Color.RED, S, E);
                                }
                                g.drawString(attributedSubString.getIterator(), 10, y);
                            }
                            y += lineHeight;
                        }

                        if (subLine.endsWith(")")) {
                            attributedString.addAttribute(TextAttribute.FONT, new Font(fontName, Font.BOLD, fontSize), subLine.length() - 4, subLine.length());
                            attributedString.addAttribute(TextAttribute.FOREGROUND, new Color(0xf58220), subLine.length() - 4, subLine.length());
                            g.drawString(attributedString.getIterator(), 10, y);
                        }

                    }
                } else {
                    while (matcher.find()) {
                        int groupCount = matcher.groupCount();
                        for (int i = 1; i <= groupCount; i++) {
                            int start = matcher.start();
                            int end = matcher.end();
                            attributedString.addAttribute(TextAttribute.FOREGROUND, Color.RED, start, end);
                        }
                        g.drawString(attributedString.getIterator(), 10, y);
                    }
                }

                if (line.endsWith(")")) {
                    attributedString.addAttribute(TextAttribute.FONT, new Font(fontName, Font.BOLD, fontSize), line.length() - 4, line.length());
                    attributedString.addAttribute(TextAttribute.FOREGROUND, new Color(0xf58220), line.length() - 4, line.length());
                    g.drawString(attributedString.getIterator(), 10, y);
                }

            } else {
                g.drawString(line, 10, y);
            }
            y += lineHeight;
        }
        g.dispose();

        ImageIO.write(image, "jpg", new File(outputImg));
    }

    /**
     * 传入选项行，返回携带翻译
     *
     * @param lineText  选项行内容
     * @param formatter 格式化字符串格式
     * @param num       1: 选项(1) (2) 这行； 2：选项(3) (4) 这行
     * @return
     * @throws IOException
     */
    public String reEnAndZh(String lineText, String formatter, int num) throws Exception {
        //选项行数据
        String[] options = new String[2];
        String[] zh = new String[2];
        String answer = "";
        if (num == 1) {
            options[0] = lineText.split(OPTION_1)[1].split(OPTION_2)[0].trim();
            options[1] = lineText.split(OPTION_2)[1].trim();
        } else if (num == 2) {
            options[0] = lineText.split(OPTION_3)[1].split(OPTION_4)[0].trim();
            options[1] = lineText.split(OPTION_4)[1].split("\\(")[0].trim();
            Pattern pattern = Pattern.compile(answerRex);
            Matcher matcher = pattern.matcher(lineText);
            if (matcher.find()) {
                answer = matcher.group();
            }
        } else {
            return "";
        }
        for (int i = 0; i < options.length; i++) {
            String option = options[i];
            //调用api翻译获取返回值
            Thread.sleep(200);
            String chinese;
            try {
                //用 try catch 增加重试次数
                chinese = invokeDicJS(option);
            } catch (Exception e) {
                e.printStackTrace();
                Thread.sleep(800);
                try {
                    chinese = invokeDicJS(option);
                } catch (Exception ex) {
                    Thread.sleep(500);
                    try {
                        chinese = invokeDicJS(option);
                    } catch (Exception exx) {
                        System.err.println("无匹配单词：" + option);
                        continue;
                    }
                }
            }
            zh[i] = chinese != null ? chinese : "";
        }
        if (num == 2) {
            return String.format(formatter, options[0], zh[0], options[1], zh[1], answer);
        } else {
            return String.format(formatter, options[0], zh[0], options[1], zh[1]);
        }
    }

    public List<Word> loadImg() {
        BufferedImage image = null;
        File file = new File(inputImg);
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        width = image.getWidth() + 150;
        height = image.getHeight() + 200;
        ITesseract instance = new Tesseract();
        // 设置语言库位置
        if ("dev".equals(env)) {
            instance.setDatapath("src/main/resources/tessdata");
        } else {
            instance.setDatapath("/usr/local/share/tessdata");
        }
        // 设置语言
        instance.setLanguage("eng");
        List<Word> words = new ArrayList<Word>();
        try {
            //读取全部数据
            instance.doOCR(image);
            System.out.println("读完数据");
            //按行读，参数i代表分割细粒度
            words = instance.getWords(image, 2);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return words;
    }

/*
    public static void aaa() {        // 创建实例
        ITesseract instance = new Tesseract();        // 设置识别语言
        instance.setLanguage("chi_sim");        // 设置识别引擎
        instance.setOcrEngineMode(1);        // 读取文件
        BufferedImage image = ImageIO.read(TestTextOcr.class.getResourceAsStream("/2.jpg"));
        try {
            //识别
            String result = instance.doOCR(image);
            System.out.println(result);
        } catch (
                TesseractException e) {
            System.err.println(e.getMessage());
        }
    }*/

    /**
     * 调用有道词典，获取单词翻译条目
     * 金山词典
     *
     * @param word
     * @return
     * @throws IOException
     */
    public synchronized String invokeDicJS(String word) throws Exception {
        word = word.replaceAll("‘", "")
                .replaceAll("’", "")
                .trim().replaceAll(" ", "%20");
       // System.out.println("word-:" + word);
        long currentTimeMillis = System.currentTimeMillis();
        JSONObject json = new JSONObject(new TreeMap<>());
        json.put("client", 6);
        json.put("key", 1000006);
        json.put("timestamp", currentTimeMillis);
        json.put("word", word);
        String t = json.values().stream().map(Object::toString).collect(Collectors.joining());
        String str = "/dictionary/word/query/web" + t + "7ece94d9f9c202b0d2ec557dg4r9bc";
        // System.out.println("str :" + str);
        String sign = MD5Util.getMD5(str);
        //System.out.println("sign:" + sign);

        String url = "http://dict.iciba.com/dictionary/word/query/web?client=6&key=1000006&timestamp=" + currentTimeMillis + "&word=" + URLEncoder.encode(word, "UTF-8") + "&signature=" + sign;
       // System.out.println("url:" + url);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", " application/json, text/plain, */*")
                .build();
        Response response = client.newCall(request).execute();
        response.header("content-type", "application/json;charset=utf-8");
        String responseData = response.body().string();
        // System.out.println(JSONObject.parseObject(responseData).toString());

        JSONArray jsonArray = JSONObject.parseObject(responseData)
                .getJSONObject("message")
                .getJSONObject("baesInfo")
                .getJSONArray("symbols");
        response.close();
        if (jsonArray != null) {
            return jsonArray.getJSONObject(0).getJSONArray("parts")
                    .getJSONObject(0).getJSONArray("means").getString(0);
        } else {
            return JSONObject.parseObject(responseData)
                    .getJSONObject("message")
                    .getJSONObject("baesInfo").getString("translate_result");
        }
    }

    /**
     * 调用有道词典，获取单词翻译条目
     * 有道词典，有些单词返回的结果不对，比如mind
     *
     * @param word
     * @return
     * @throws IOException
     */

//    public static JSONArray invokeDicYD(String word) throws IOException {
//        OkHttpClient client = new OkHttpClient();
//
//        FormBody form = new FormBody.Builder()
//                .add("q", word)
//                .add("le", "en")
//                .add("t", "1")
//                .add("client", "web")
//                .add("sign", "xxx")
//                .add("keyfrom", "webdict")
//                .build();
//
//        Request request = new Request.Builder()
//                .url("https://dict.youdao.com/jsonapi_s?doctype=json&jsonversion=4")
//                .method("POST", form)
//                .addHeader("Accept", " application/json, text/plain, */*")
////                .addHeader("Accept-Encoding", " gzip, deflate, br")
////                .addHeader("Accept-Language", " zh-CN,zh;q=0.9")
//                .addHeader("Connection", " keep-alive")
//                .addHeader("Cookie", "OUTFOX_SEARCH_USER_ID=1741207844@114.252.43.107")
//                .build();
//
//        Response response = client.newCall(request).execute();
//        response.header("content-type", "application/json;charset=utf-8");
//        String responseData = response.body().string();
//        System.out.println("word-:" + word);
//        System.out.println(JSONObject.parseObject(responseData).toString());
//        return JSONObject.parseObject(responseData)
//                .getJSONObject("ec")
//                .getJSONObject("word")
//                .getJSONArray("trs");
//    }

}
