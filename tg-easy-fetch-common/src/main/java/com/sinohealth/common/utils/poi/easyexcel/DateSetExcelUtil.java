package com.sinohealth.common.utils.poi.easyexcel;


import com.alibaba.fastjson.JSON;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Huangzk
 * @date 2021/8/24 15:59
 */
@Slf4j
public class DateSetExcelUtil {


    /**
     * 用来验证excel与Vo中的类型是否一致 <br>
     * Map<栏位类型,只能是哪些Cell类型>
     */
    private static final Map<Class<?>, CellType[]> validateMap = new HashMap<>();

    /**
     * 数据库映射
     */
    private static final Map<String, CellType[]> sqlTypeMap = new HashMap<>();

    /**
     * 注意要除掉表头的2行
     */
    public static final int MAX_IMPORT_ROWS = 10002;

    public static final String F_VARCHAR = "varchar";
    public static final String F_DOUBLE = "double";
    public static final String F_TIMESTAMP = "datetime";
    public static final String F_INT = "int";

    static {
        validateMap.put(String[].class, new CellType[]{CellType.STRING});
        validateMap.put(Double[].class, new CellType[]{CellType.NUMERIC});
        validateMap.put(String.class, new CellType[]{CellType.STRING});
        validateMap.put(Double.class, new CellType[]{CellType.NUMERIC});
        validateMap.put(Date.class, new CellType[]{CellType.NUMERIC, CellType.STRING});
        validateMap.put(Integer.class, new CellType[]{CellType.NUMERIC});
        validateMap.put(Float.class, new CellType[]{CellType.NUMERIC});
        validateMap.put(Long.class, new CellType[]{CellType.NUMERIC});
        validateMap.put(Boolean.class, new CellType[]{CellType.BOOLEAN});
    }

    static {
        sqlTypeMap.put(F_VARCHAR, new CellType[]{CellType.STRING});
        sqlTypeMap.put(F_DOUBLE, new CellType[]{CellType.NUMERIC});
        sqlTypeMap.put(F_TIMESTAMP, new CellType[]{CellType.NUMERIC, CellType.STRING});
        sqlTypeMap.put(F_INT, new CellType[]{CellType.NUMERIC});
        //sqlTypeMap.put("bigint", new CellType[]{CellType.NUMERIC});
    }


    public static <T> List<T> importExcel(Class<T> clazz, InputStream inputStream, String pattern) {
        Workbook workBook;
        try {
            workBook = WorkbookFactory.create(inputStream);
        } catch (Exception e) {
            log.error("load excel file error", e);
            return null;
        }
        List<T> list = new ArrayList<>();
        Sheet sheet = workBook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();

        // Map<title,index>
        Map<String, Integer> titleMap = new LinkedHashMap<>();

        int sumRow = 0;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            sumRow++;
            if (sumRow > MAX_IMPORT_ROWS) {
                throw new CustomException("数据量>1万，上传失败");
            }

            try {
                if (row.getRowNum() == 0) {
                    if (clazz == Map.class) {
                        // 解析map用的key,就是excel标题行
                        Iterator<Cell> cellIterator = row.cellIterator();
                        Integer index = 0;
                        while (cellIterator.hasNext()) {
                            String value = cellIterator.next().getStringCellValue();
                            titleMap.put(value, index);
                            index++;
                        }
                    }
                    continue;
                }
                // 整行都空，就跳过
                boolean allRowIsNull = true;
                Iterator<Cell> cellIterator = row.cellIterator();
                while (cellIterator.hasNext()) {
                    Object cellValue = getCellValue(cellIterator.next());
                    if (cellValue != null) {
                        allRowIsNull = false;
                        break;
                    }
                }
                if (allRowIsNull) {
                    log.warn("Excel row " + row.getRowNum() + " all row value is null!");
                    continue;
                }
                if (clazz == Map.class) {
                    Map<String, Object> map = new LinkedHashMap<>();
                    for (String k : titleMap.keySet()) {
                        Integer index = titleMap.get(k);
                        Cell cell = row.getCell(index);
                        // 判空
                        if (cell == null) {
                            map.put(k, null);
//                        } else if (row.getRowNum() < 3) {      //前三行格式为字段名,字段别名,主键
                        } else if (row.getRowNum() < 2) {      //前三行格式为字段名,字段别名,主键
                            cell.setCellType(CellType.STRING);
                            String value = cell.getStringCellValue();
                            map.put(k, value);
                        } else {
                            int rowNum = row.getRowNum() + 1;
                            String column = CellReference.convertNumToColString(cell.getColumnIndex());
                            Object cellValue = getCellValue(cell);
//                            if (row.getRowNum() == 3) {      //以第四行类型为准生成每列的映射
                            if (row.getRowNum() == 2) {      //以第四行类型为准生成每列的映射
                                //此处不允许为空
                                if (cellValue == null) {
                                    throw new CustomException(String.format("{%d}行{%s}列参数类型解析失败!", rowNum, column));
                                }
                                processSqlType(cellValue, k);
                            }
                            String errMsg = validateCell(cell, (String) SqlTypeContext.get(k), index);
                            if (isBlank(errMsg)) {
                                // 处理特殊情况,Excel中的String,转换成Date
                                if (StringUtils.equals((String) SqlTypeContext.get(k), F_TIMESTAMP)
                                        && cell.getCellType() == CellType.STRING) {
                                    Object strDate = getCellValue(cell);
                                    try {
                                        cellValue = new SimpleDateFormat(pattern).parse(strDate.toString());
                                    } catch (ParseException e) {

                                        errMsg = MessageFormat.format("the cell [{0}],[{1}] can not be converted to a date ",
                                                rowNum, column);
                                    }
                                }
                                map.put(k, cellValue);
                            }
                            if (isNotBlank(errMsg)) {
                                throw new CustomException(errMsg);
                            }

                        }
                    }
                    list.add((T) map);
                }
            } catch (Exception e) {
                if (e instanceof CustomException) {
                    throw e;
                }
                throw new CustomException(MessageFormat.format("can not process:{0}",
                        row.getRowNum() + 1), e);
            }
        }

        return list;
    }


    private static void processSqlType(Object cellValue, String title) {
        String sqlType = null;
        if (cellValue instanceof String) {
            sqlType = F_VARCHAR;
        } else if (cellValue instanceof RichTextString) {
            sqlType = F_VARCHAR;
        } else if (cellValue instanceof Double) {
            sqlType = F_DOUBLE;
        } else if (cellValue instanceof Date) {
            sqlType = F_TIMESTAMP;
        } else if (cellValue instanceof Long) {
            sqlType = F_INT;
        }
        SqlTypeContext.put(title, sqlType);
    }


    /**
     * 获取单元格值
     *
     * @param cell
     * @return
     */
    public static Object getCellValue(Cell cell) {
        if (cell == null
                || (cell.getCellType() == CellType.STRING && isBlank(cell
                .getStringCellValue()))) {
            return null;
        }
        CellType cellType = cell.getCellType();
        if (cellType == CellType.BLANK)
            return null;
        else if (cellType == CellType.BOOLEAN)
            return cell.getBooleanCellValue();
        else if (cellType == CellType.ERROR)
            return cell.getErrorCellValue();
        else if (cellType == CellType.FORMULA) {
            try {
                if (HSSFDateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    return cell.getNumericCellValue();
                }
            } catch (IllegalStateException e) {
                return cell.getRichStringCellValue();
            }
        } else if (cellType == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                return DateUtils.dateTime(cell.getDateCellValue());
            } else {
                return Math.round(cell.getNumericCellValue());
            }
        } else if (cellType == CellType.STRING)
            return cell.getStringCellValue();
        else
            return null;
    }


    /**
     * 验证Cell类型是否正确
     *
     * @param cell    cell单元格
     * @param field   比较值
     * @param cellNum 列,用于errMsg
     * @return
     */
    private static String validateCell(Cell cell, String field, int cellNum) {
        String columnName = CellReference.convertNumToColString(cellNum);
        String result = null;
        CellType[] cellTypeArr = sqlTypeMap.get(field);
        if (cellTypeArr == null) {
            result = MessageFormat.format("the cell [{0}],[{1}] Unsupported type [{2}]", cell.getRow().getRowNum(), columnName, field);
            return result;
        }
        if (cell.getCellType() == CellType.BLANK) {
            return result;
        } else {
            //List<CellType> cellTypes = Arrays.asList(cellTypeArr);
            //
            //// 如果類型不在指定範圍內,並且沒有默認值
            //if (!(cellTypes.contains(cell.getCellType()))) {
            //    StringBuilder strType = new StringBuilder();
            //    for (int i = 0; i < cellTypes.size(); i++) {
            //        CellType cellType = cellTypes.get(i);
            //        strType.append(getCellTypeByInt(cellType));
            //        if (i != cellTypes.size() - 1) {
            //            strType.append(",");
            //        }
            //    }
            //    result = MessageFormat.format("the cell [{0}],[{1}] type must [{2}]", cell.getRow().getRowNum(), columnName, strType.toString());
            //}
        }
        return result;
    }


    /**
     * 获取cell类型的文字描述
     *
     * @param cellType CellType.BLANK
     *                 CellType.BOOLEAN
     *                 CellType.ERROR
     *                 CellType.FORMULA
     *                 CellType.NUMERIC
     *                 CellType.STRING
     * @return
     */
    private static String getCellTypeByInt(CellType cellType) {
        if (cellType == CellType.BLANK)
            return "Null type";
        else if (cellType == CellType.BOOLEAN)
            return "Boolean type";
        else if (cellType == CellType.ERROR)
            return "Error type";
        else if (cellType == CellType.FORMULA)
            return "Formula type";
        else if (cellType == CellType.NUMERIC)
            return "Numeric type";
        else if (cellType == CellType.STRING)
            return "String type";
        else
            return "Unknown type";
    }


    private static boolean isBlank(String str) {
        if (str == null) {
            return true;
        }
        return str.length() == 0;
    }

    protected static boolean isNotBlank(String str) {
        return !isBlank(str);
    }


    public static boolean getFileExtension(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new CustomException("文件不能为空!");
        String filename = file.getOriginalFilename();

        if (StringUtils.isBlank(filename)) {
            throw new CustomException("文件名不合法!");
        }
        int idx = filename.lastIndexOf('.');
        if (idx != -1) {
            String expand = filename.substring(idx + 1).trim().toLowerCase();
            if (expand.equalsIgnoreCase("xls") || expand.equalsIgnoreCase("xlsx") || expand.equalsIgnoreCase("csv"))
                return true;
        }
        return false;
    }

    public static void main(String[] args) throws FileNotFoundException {
        File f = new File("D:\\studysource\\ExcelUtil\\src\\test\\resources\\test.xls");
        InputStream inputStream = new FileInputStream(f);
        List<Map> maps = importExcel(Map.class, inputStream, "yyyy/MM/dd HH:mm:ss");
        for (Map m : maps) {
            System.out.println(m);
        }
        Map<String, Object> stringObjectMap = SqlTypeContext.context.get();
        System.out.println("context = " + JSON.toJSONString(stringObjectMap));
    }
}