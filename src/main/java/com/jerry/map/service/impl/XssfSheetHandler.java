package com.jerry.map.service.impl;

import com.google.common.collect.Maps;
import com.jerry.map.utils.Constants;
import com.jerry.map.utils.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2016/1/26.
 */
public class XssfSheetHandler extends DefaultHandler {

    /**
     * The type of the data value is indicated by an attribute on the cell.
     * The value is usually in a "v" element within the cell.
     */
    enum xssfDataType {
        BOOL,
        ERROR,
        FORMULA,
        INLINESTR,
        SSTINDEX,
        NUMBER,
    }

    /**
     * Table with styles
     */
    private StylesTable stylesTable;
    /**
     * Table with unique strings
     */
    private ReadOnlySharedStringsTable sharedStringsTable;
    /**
     * Number of columns to read starting with leftmost
     */
    private final int minColumnCount;
    // Set when V start element is seen
    private boolean vIsOpen;
    // Set when cell start element is seen;
    // used when cell close element is seen.
    private xssfDataType nextDataType;
    // Used to format numeric cell values.
    private short formatIndex;
    private String formatString;
    private final DataFormatter formatter;
    // 定义当前读到的列数，实际读取时会按照从0开始...
    private int thisColumn = -1;
    // 定义上一次读到的列序号
    private int lastColumnNumber = -1;
    // Gathers characters as they are seen.
    private StringBuffer value;
    // 定义存储每行内容的list
    private List<String> rowlist = new ArrayList<String>();
    // 定义当前读到的列与上一次读到的列中是否有空值（即该单元格什么也没有输入，连空格都不存在）默认为false
    private boolean flag = false;

    // 当前行
    private int curRow = 0;

    private int sheetIndex = 0;


    private Map<String, String> cateMap = Maps.newHashMap();

    /**
     * Accepts objects needed while parsing.
     *
     * @param styles  Table of styles
     * @param strings Table of shared strings
     * @param cols    Minimum number of columns to show
     */
    public XssfSheetHandler(
            StylesTable styles,
            ReadOnlySharedStringsTable strings,
            int cols) {
        this.stylesTable = styles;
        this.sharedStringsTable = strings;
        this.minColumnCount = cols;
        this.value = new StringBuffer();
        this.nextDataType = xssfDataType.NUMBER;
        this.formatter = new DataFormatter();
    }


    /* 业务逻辑实现方法
         * @see com.eprosun.util.excel.IRowReader#getRows(int, int, java.util.List)
         */
    public void getRows(int sheetIndex, int curRow, List<String> rowlist) {


        if (curRow >= 4 && rowlist.size() > 0) {
            getValue(rowlist);
        }
    }

    /*
       * (non-Javadoc)
       * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
       */
    public void startElement(String uri, String localName, String name,
                             Attributes attributes) throws SAXException {

        if ("inlineStr".equals(name) || "v".equals(name)) {
            vIsOpen = true;
            // Clear contents cache
            value.setLength(0);
        }
        // c => cell
        else if ("c".equals(name)) {
            // Get the cell reference
            String r = attributes.getValue("r");
            int firstDigit = -1;
            for (int c = 0; c < r.length(); ++c) {
                if (Character.isDigit(r.charAt(c))) {
                    firstDigit = c;
                    break;
                }
            }
            thisColumn = nameToColumn(r.substring(0, firstDigit));//获取当前读取的列数

            // Set up defaults.
            this.nextDataType = xssfDataType.NUMBER;
            this.formatIndex = -1;
            this.formatString = null;
            String cellType = attributes.getValue("t");
            String cellStyleStr = attributes.getValue("s");
            if ("b".equals(cellType)) {
                nextDataType = xssfDataType.BOOL;
            } else if ("e".equals(cellType)) {
                nextDataType = xssfDataType.ERROR;
            } else if ("inlineStr".equals(cellType)) {
                nextDataType = xssfDataType.INLINESTR;
            } else if ("s".equals(cellType)) {
                nextDataType = xssfDataType.SSTINDEX;
            } else if ("str".equals(cellType)) {
                nextDataType = xssfDataType.FORMULA;
            } else if (cellStyleStr != null) {
                // It's a number, but almost certainly one
                //  with a special style or format
                int styleIndex = Integer.parseInt(cellStyleStr);
                XSSFCellStyle style = stylesTable.getStyleAt(styleIndex);
                this.formatIndex = style.getDataFormat();
                this.formatString = style.getDataFormatString();
                if (this.formatString == null) {
                    this.formatString = BuiltinFormats.getBuiltinFormat(this.formatIndex);
                }
            }
        }
    }

    /*
       * (non-Javadoc)
       * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
       */
    public void endElement(String uri, String localName, String name)
            throws SAXException {

        String thisStr = null;

        // v => contents of a cell
        if ("v".equals(name)) {
            // Process the value contents as required.
            // Do now, as characters() may be called more than once
            switch (nextDataType) {
                case BOOL:
                    char first = value.charAt(0);
                    thisStr = first == '0' ? "FALSE" : "TRUE";
                    break;
                case ERROR:
                    thisStr = "\"ERROR:" + value.toString() + '"';
                    break;
                case FORMULA:
                    // A formula could result in a string value,
                    // so always add double-quote characters.
                    thisStr = '"' + value.toString() + '"';
                    break;
                case INLINESTR:
                    // TODO: have seen an example of this, so it's untested.
                    XSSFRichTextString rtsi = new XSSFRichTextString(value.toString());
                    thisStr = '"' + rtsi.toString() + '"';
                    break;
                case SSTINDEX:
                    String sstIndex = value.toString();
                    try {
                        int idx = Integer.parseInt(sstIndex);
                        XSSFRichTextString rtss = new XSSFRichTextString(sharedStringsTable.getEntryAt(idx));
                        //                            thisStr = '"' + rtss.toString() + '"';
                        thisStr = rtss.toString();
                    } catch (NumberFormatException ex) {
                        System.out.println("Failed to parse SST index '" + sstIndex + "': " + ex.toString());
                    }
                    break;
                case NUMBER:
                    String n = value.toString();
                    if (this.formatString != null) {
                        thisStr = formatter.formatRawCellContents(Double.parseDouble(n), this.formatIndex, this.formatString);
                    } else {
                        thisStr = n;
                    }
                    break;
                default:
                    thisStr = "(TODO: Unexpected type: " + nextDataType + ")";
                    break;
            }

            // Output after we've seen the string contents
            // Emit commas for any fields that were missing on this row
                 /*if (lastColumnNumber == -1) {
                     lastColumnNumber = 0;
                 }*/
            // 以下是核心算法，在同一行内，若后一次比前一次读取的列序号相差大于1，证明中间没有读到值
            // 按照.xlsx底层是xml描述文件原理，此时对应xml中"空值"情况
            if (thisColumn - lastColumnNumber > 1) {
                flag = true;
            }
            for (int i = lastColumnNumber; i < thisColumn; ++i) {
                if (flag && i > lastColumnNumber) {
                    rowlist.add(i, "");
                }
            }

            // Might be the empty string.
            rowlist.add(thisColumn, thisStr.trim());

            // Update column
            if (thisColumn > -1) {
                lastColumnNumber = thisColumn;
            }

        } else if ("row".equals(name)) {//读到一行末尾

            // Print out any missing commas if needed
//            if (minColumnCount > 0) {
//                // Columns are 0 based
//                if (lastColumnNumber == -1) {
//                    lastColumnNumber = 0;
//                }
//                for (int i = lastColumnNumber; i < (this.minColumnCount); i++) {
//                    System.out.println("");
//                }
//            }
            getRows(sheetIndex, curRow, rowlist);
            rowlist.clear();
            curRow++;
            flag = false;
            // We're onto a new row
            lastColumnNumber = -1;
        }
    }

    /**
     * Captures characters only if a suitable element is open.
     * Originally was just "v"; extended for inlineStr also.
     */
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (vIsOpen) {
            value.append(ch, start, length);
        }
    }

    /**
     * Converts an Excel column name like "C" to a zero-based index.
     *
     * @param name
     * @return Index corresponding to the specified name
     */
    private int nameToColumn(String name) {
        int column = -1;
        for (int i = 0; i < name.length(); ++i) {
            int c = name.charAt(i);
            column = (column + 1) * 26 + c - 'A';
        }
        return column;
    }


    public Map<String, String> getMap() {
        return cateMap;
    }

    @SuppressWarnings("static-access")
    private void getValue(List row) {

        String catFirstLevel = WordUtils.normalize(row.get(0).toString());
        String catSecondLevel = WordUtils.normalize(row.get(1).toString());
        String catThirdLevel = WordUtils.normalize(row.get(2).toString());
        String catForthLevel = WordUtils.normalize(row.get(5).toString());
        String catSynonyms = row.get(6).toString();








        if (StringUtils.isNotEmpty(catFirstLevel)) {
            cateMap.put(catFirstLevel, Constants.CATEGORY);
        }
        if (StringUtils.isNotEmpty(catSecondLevel)) {
            cateMap.put(catSecondLevel, Constants.CATEGORY);
        }
        if (StringUtils.isNotEmpty(catThirdLevel)) {
            cateMap.put(catThirdLevel, Constants.CATEGORY);
        }
        if (StringUtils.isNotEmpty(catForthLevel)) {
            cateMap.put(catForthLevel, Constants.BRAND);
        }

        if (StringUtils.isNotEmpty(catSynonyms)) {
            List<String> synonyms = WordUtils.str2List(catSynonyms, "-");
            if (StringUtils.isNotEmpty(catForthLevel)) {
                for (String synonym : synonyms) {
                    cateMap.put(WordUtils.normalize(synonym), Constants.BRAND);
                }
            } else {
                for (String synonym : synonyms) {
                    cateMap.put(WordUtils.normalize(synonym), Constants.CATEGORY);
                }
            }
        }
    }
}
