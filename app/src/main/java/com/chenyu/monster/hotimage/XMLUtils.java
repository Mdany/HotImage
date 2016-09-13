package com.chenyu.monster.hotimage;

import android.content.Context;
import android.text.TextUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by chenyu on 16/6/22.
 */
public class XMLUtils {
    private static volatile XMLUtils mXmlUtils;
    private Context mContext;

    public XMLUtils(Context mContext) {
        this.mContext = mContext;
    }

    public static XMLUtils getInstance(Context context) {
        if (mXmlUtils == null) {
            synchronized (XMLUtils.class) {
                if (mXmlUtils == null) {
                    mXmlUtils = new XMLUtils(context);
                }
            }
        }
        return mXmlUtils;
    }

    /**
     * 根据fileName返回dom树
     *
     * @param fileName
     * @return
     */
    public BodyArea readDoc(String fileName) {
        InputStream inputStream = null;
        BodyArea root = null;
        try {
            inputStream = new FileInputStream(fileName);
            root = readDoc(inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            FileUtils.closeInputStream(inputStream);
        }
        return root;
    }

    /**
     * 从文件流里读取xml文档
     *
     * @param inputStream
     * @return
     */
    public BodyArea readDoc(InputStream inputStream) {
        Document doc = null;
        BodyArea root = new BodyArea();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            doc = builder.parse(inputStream);
            NodeList nodeList = doc.getChildNodes();
            if (nodeList != null && nodeList.getLength() == 1) {
                Node node = nodeList.item(0);
                root = readToPlace(null, node);
            }
        } catch (Exception e) {
        }
        return root;
    }

    /**
     * 递归读取xml节点并实例化为Area
     *
     * @param placeNode
     * @param node
     * @return
     */
    public BodyArea readToPlace(BodyArea placeNode, Node node) {
        BodyArea hNode = placeNode;
        if (hNode == null)
            hNode = new BodyArea();
        Element element = (Element) node;
        NamedNodeMap attrs = element.getAttributes();
        int len = attrs.getLength();
        for (int i = 0; i < len; i++) {
            Node n = attrs.item(i);
            String name = n.getNodeName();
            String value = n.getNodeValue();
            if ("areaId".equals(name)) {
                hNode.areaId = value;
            }else if ("bodyPart".equals(name)){
                hNode.bodyPart = value;
            }else if ("areaTitle".equals(name)) {
                hNode.areaTitle = value;
            } else if ("pts1".equals(name)) {
                hNode.setPts1(value, ",");
            } else if ("pts2".equals(name)) {
                hNode.setPts2(value, ",");
            } else if ("desc".equals(name)) {
                hNode.desc = value;
            }
        }

        NodeList nodeList = element.getChildNodes();
        int jLen = nodeList.getLength();
        for (int j = 0; j < jLen; j++) {
            Node n = nodeList.item(j);
            if (n instanceof Element) {
                BodyArea b = new BodyArea();
                hNode.areas.add(b);
                readToPlace(b, n);
            }
        }
        return hNode;
    }

    public static class Parent {
        protected String title;
        protected String desc;
        protected String code;
        protected List<Child> places;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public List<Child> getPlaces() {
            return places;
        }

        public void setPlaces(List<Child> places) {
            this.places = places;
        }
    }

    public static class Root extends Parent {
        protected String path;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

    public static class Child extends Parent {
        private String pointStr;
        private final List<Integer> points = new ArrayList<>();

        public String getPointStr() {
            return pointStr;
        }

        public void setPointStr(String pointStr) {
            this.pointStr = pointStr;
        }

        public List<Integer> getPoints() {
            return points;
        }

        /**
         * 一个坐标组成字符串转为字符数组并赋值
         *
         * @param pointStr "1,3,4,5,6"
         */
        public void setPoints(String pointStr) {
            if (!TextUtils.isEmpty(pointStr)) {
                String[] points = pointStr.split(",");
                for (String point : points) {
                    this.points.add(Integer.valueOf(point));
                }
            }
        }
    }
}
