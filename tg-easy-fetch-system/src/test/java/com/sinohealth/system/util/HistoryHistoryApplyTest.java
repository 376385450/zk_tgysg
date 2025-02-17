package com.sinohealth.system.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sinohealth.common.utils.JsonUtils;
import com.sinohealth.common.utils.poi.ExcelUtil;
import com.sinohealth.system.biz.application.dto.HistoryProject;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.dto.analysis.FilterDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static com.sinohealth.system.util.HistoryApplyUtil.*;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-09-06 14:44
 */
@Slf4j
public class HistoryHistoryApplyTest {

    private Map<String, HistoryProject> projectMap = Collections.emptyMap();
    private List<HistoryProject> projects = Collections.emptyList();
    //    private static final String sourceExcel = "/home/zk/Documents/Project/Easy-Fetch/线下需求文档.read.xlsx";
    private static final String sourceExcel = "/home/zk/Documents/Project/Easy-Fetch/线下需求文档0914.xlsx";

    @Test
    public void testExcelToJson() throws Exception {
        ExcelUtil<HistoryProject> excelUtil = new ExcelUtil<>(HistoryProject.class);
        projects = excelUtil.importExcel(Files.newInputStream(new File(sourceExcel).toPath()));

        String json = JsonUtils.format(projects);
        System.out.println(json);
        assert json != null;
        Files.write(Paths.get("data.json"), json.getBytes(StandardCharsets.UTF_8));
    }

    @Before
    public void initProjectMap() throws Exception {
        byte[] bytes = Files.readAllBytes(Paths.get("data.json"));
        projects = JsonUtils.parse(new String(bytes), new TypeReference<List<HistoryProject>>() {
        });

        this.projectMap = Lambda.buildMap(projects, HistoryProject::getId, v -> v);
    }

    @Test
    public void testWriteExcel() throws Exception {
        writeResult(Collections.emptyList());
    }

    private void testParse238Model(String sql) {
        FilterDTO filter = HistoryApplyUtil.parseSql(sql);
        System.out.println(HistoryApplyUtil.format(filter));
        String result = buildSql(filter);
        printFmtSql(HistoryApplyUtil.prefix + result);
    }

//    private void testParseVisitorModel(String sql) {
//        FilterDTO filter = HistoryApplyUtil.parseByVisitor(sql);
//        if (Objects.isNull(filter)) {
//            Assert.fail();
//        }
////        System.out.println(HistoryProjectUtil.format(filter));
//        String result = buildSql(filter);
//        printFmtSql(HistoryApplyUtil.prefix + result);
//    }

    // TODO 1. 处理多层SQL 2. 处理238 SQL正确性
    @Test
    public void testSimpleSQL() throws Exception {
        // TODO 第一部分问题

        // Op 类型 false 则是容器节点。当返回的时候，需要提取出子节点（容器类型）到当前层
//        testParse238Model("(a = 1 and b=1 and ((c=1 and (d=1 and e=1)) and f=1) and g=1 and h=1 and j=1)");
//        testParse238Model("(a = 1 and b=1 and c=1 and d=1 or (e=1 and f=1 and g=1 and h=1 and j=1))");

//        testParse238Model("(a = 1 or (xls=8 and x=8))");
//        testParse238Model("((a = 1 and c =3) or b=2)");
//        testParse238Model("(( cj = 4 and cj=7) or (xls=8 and x=8))");
//        testParse238Model("(( cj = 4 and cj=7) or (xls=8 and x=8 and c =9))");
//
//        testParse238Model("((a = 1 and b = 2 and c = 3) or ( d = 4 and e=7 and f = 5) or (xls=8 and x=8))");
//        testParse238Model("((a = 1 and b = 2 and c = 3 and d =4) or ( d = 4 and e=7 and f = 5) or (xls=8 and x=8 and b=5)  or (xls=5 and x=3 and x=1))");

        // 同树，直到遇到true才合并
        // （2 3 叶子同层 true。） 1 中2同层 true  中3层 false
        // (4 7 叶子同层 false) 5 中2同层 false 中3层true
//        testParse238Model("((a = 1 and ( b = 2 or c = 3)) or ( d = 4 and e=7 and f = 5) or (xls=8 and x=8))");
//        testParse238Model("(( prodcode = 'P002' and cj like '%以岭%' and pm like '%连花清瘟%' ) or ( prodcode = 'P002' and cj like '%华润三九%' and pm like '%感冒灵%' ) or ( prodcode = 'P002' and cj like '%太极集团重庆涪陵%' and pm like '%藿香%' ) or ( prodcode = 'P023' and cj like '%济川%' and pm like '%蒲地蓝消炎口服液%' ))");

//        testParse238Model("((a = 1 and b = 2 and c = 3 and g=8) or ( d = 4 and e=7 and f = 5) or (xls=8 and x=8))");

//        testParse238Model("(prodcode='P025' and tym in ('环孢素','甲氨蝶呤') ) or (prodcode='P012' and tym in('阿达木单抗','戈利木单抗','依那西普','英夫利西单抗','巴瑞替尼','托法替布')) or (prodcode='P015' and tym in ('阿维A','复方氨肽素','甲氧沙林','维A酸','乌司奴单抗','依奇珠单抗','古塞奇尤单抗','阿普米司特','司库奇尤单抗','阿布昔替尼','乌帕替尼'))");

//        testParse238Model("(  prodcode = 'P024' and sort1 = '眼科类' and  otc_rx =  '其他' )");

//        // 追加否定条件后，前端错误
//        testParse238Model("((prodcode = 'P007' and  cj  like '%以岭%') or\n" +
//                "(prodcode = 'P007' and  pm  like '%复方丹参滴丸%' and  cj  like '%天士力%') or\n" +
//                "(prodcode = 'P007' and  pm  like '%脑心通胶囊%' and  cj  like '%步长%') or\n" +
//                "(prodcode = 'P007' and  pm ='稳心颗粒' and  cj  like '%步长%')) AND    brand not in ('丹七片(石家庄以岭药业股份有限公司)','单硝酸异山梨酯片(石家庄以岭药业股份有限公司)','银杏叶片(石家庄以岭药业股份有限公司)') ");


        testParse238Model("( prodcode = 'P012' and tym ~ '氨基葡萄糖' ) or ( prodcode = 'P012' and jx ~ '灌肠剂|喷剂|软膏与乳膏剂|栓剂|贴剂|注射剂' ) or ( prodcode = 'P012' and sort3= '回收站') or ( prodcode = 'P012' and sort3= '解痉药')");


        // 前端处理后的JSON
//        String x = "{\"id\":\"510ccb5e-7906-4149-82fe-66b46b29a2d2\",\"fatherId\":\"6c381255-5582-448e-9603-d2aa0938529b\",\"filters\":[{\"fatherId\":\"510ccb5e-7906-4149-82fe-66b46b29a2d2\",\"isFather\":1,\"filterItem\":{\"id\":\"c9240878-1953-4d99-b544-9fe0f7b54647\",\"fatherId\":\"6c381255-5582-448e-9603-d2aa0938529b\",\"filters\":[{\"id\":\"e58a3bcf-47ad-4f3d-9775-ce6975d567ef\",\"fatherId\":\"c9240878-1953-4d99-b544-9fe0f7b54647\",\"filters\":[{\"fatherId\":\"e58a3bcf-47ad-4f3d-9775-ce6975d567ef\",\"isFather\":1,\"filterItem\":{\"id\":\"33f38f32-1aa9-4473-bc19-cb657b367406\",\"fatherId\":\"c9240878-1953-4d99-b544-9fe0f7b54647\",\"filters\":[{\"id\":\"f55cc0cb-c4c5-40d3-b484-6067a43cf4bb\",\"fatherId\":\"33f38f32-1aa9-4473-bc19-cb657b367406\",\"filters\":[{\"logicalOperator\":\"and\",\"filterItem\":{\"fieldName\":\"cj\",\"andOr\":\"and\",\"functionalOperator\":\"equalTo\",\"value\":\"1\"},\"id\":\"9b1b6386-789f-45f6-b61c-121cb7fc475a\",\"fatherId\":\"33f38f32-1aa9-4473-bc19-cb657b367406\"},{\"logicalOperator\":\"and\",\"filterItem\":{\"fieldName\":\"cj\",\"andOr\":\"and\",\"functionalOperator\":\"equalTo\",\"value\":\"2\"},\"id\":\"6e41baad-0418-40a4-971f-e2a8276aed7e\",\"fatherId\":\"33f38f32-1aa9-4473-bc19-cb657b367406\"},{\"logicalOperator\":\"and\",\"filterItem\":{\"fieldName\":\"cj\",\"andOr\":\"and\",\"functionalOperator\":\"equalTo\",\"value\":\"3\"},\"id\":\"4d887449-a129-4eb9-88a5-66e2c73ef23d\",\"fatherId\":\"33f38f32-1aa9-4473-bc19-cb657b367406\"}]}]}},{\"fatherId\":\"e58a3bcf-47ad-4f3d-9775-ce6975d567ef\",\"isFather\":1,\"filterItem\":{\"id\":\"b6938312-ac49-430a-b480-915b6341d8aa\",\"fatherId\":\"c9240878-1953-4d99-b544-9fe0f7b54647\",\"filters\":[{\"id\":\"b89b38a9-d007-44ee-ad82-cee4401e6579\",\"fatherId\":\"b6938312-ac49-430a-b480-915b6341d8aa\",\"filters\":[{\"logicalOperator\":\"and\",\"filterItem\":{\"fieldName\":\"cj\",\"andOr\":\"and\",\"functionalOperator\":\"equalTo\",\"value\":\"4\"},\"id\":\"388de96e-5d07-4d4c-93b4-713d88727c5f\",\"fatherId\":\"b6938312-ac49-430a-b480-915b6341d8aa\"},{\"logicalOperator\":\"and\",\"filterItem\":{\"fieldName\":\"cj\",\"andOr\":\"and\",\"functionalOperator\":\"equalTo\",\"value\":\"7\"},\"id\":\"10e9bee3-0692-4dcd-a5a7-04194af6c113\",\"fatherId\":\"b6938312-ac49-430a-b480-915b6341d8aa\"}]}]}}]}]}},{\"fatherId\":\"510ccb5e-7906-4149-82fe-66b46b29a2d2\",\"isFather\":1,\"filterItem\":{\"id\":\"b070d1da-494e-4ef7-81a8-71b4e21f2eed\",\"fatherId\":\"6c381255-5582-448e-9603-d2aa0938529b\",\"filters\":[{\"id\":\"1d44e67b-2bf1-415b-9ceb-ee61791b132a\",\"fatherId\":\"b070d1da-494e-4ef7-81a8-71b4e21f2eed\",\"filters\":[{\"logicalOperator\":\"and\",\"filterItem\":{\"fieldName\":\"xls\",\"andOr\":\"and\",\"functionalOperator\":\"equalTo\",\"value\":\"8\"},\"id\":\"e3ef3928-9a31-4c68-a94c-ef59b8ccc5da\",\"fatherId\":\"b070d1da-494e-4ef7-81a8-71b4e21f2eed\"},{\"logicalOperator\":\"and\",\"filterItem\":{\"fieldName\":\"x\",\"andOr\":\"and\",\"functionalOperator\":\"equalTo\",\"value\":\"8\"},\"id\":\"826a5055-9963-4a87-8045-81d3737f1f95\",\"fatherId\":\"b070d1da-494e-4ef7-81a8-71b4e21f2eed\"}]}]}}]}";
//        FilterDTO filterDTO = JsonUtils.parse(x, FilterDTO.class);
//        tryBuildSQL(filterDTO);
    }

//    @Test
//    public void testVisitorMode() throws Exception {
//        testParseVisitorModel("(( prodcode = 'P002' and cj like '%以岭%' and pm like '%连花清瘟%' ) \n" +
//                "or ( prodcode = 'P002' and cj like '%华润三九%' and pm like '%感冒灵%' ) \n" +
//                "or ( prodcode = 'P002' and cj like '%太极集团重庆涪陵%' and pm like '%藿香%' ) \n" +
//                "or ( prodcode = 'P023' and cj like '%济川%' and pm like '%蒲地蓝消炎口服液%' ))");
//    }

    @Test
    public void testOneUnit() throws Exception {
        // 常见两级条件组
//        HistoryProject project = projectMap.get("530");
//        HistoryProject project = projectMap.get("238");
        // 取反 正则 ~
        HistoryProject project = projectMap.get("84");

        // 单条件
//        Project project = projectMap.get("795");

        // in 756
//        Project project = projectMap.get("766");

        FilterDTO filter = HistoryApplyUtil.parse(project);
        project.setFilter(HistoryApplyUtil.format(filter));
        log.info("filter={}\n\n", HistoryApplyUtil.format(filter));
        buildSql(filter);
    }

    @Test
    public void testParseAll() throws Exception {
        for (HistoryProject project : projects) {
            try {
                FilterDTO pro238 = HistoryApplyUtil.parse(project);
                project.setFilter(HistoryApplyUtil.format(pro238));
            } catch (Exception e) {
                log.error("", e);
                project.setFilter("ERROR");
            }
        }

        writeResult(projects);
    }


    @Test
    public void testAx() throws Exception {
        FilterDTO a = HistoryApplyUtil.parseSql(" ((ptype = '' and ((((dtype = '' and ttype = '') or product_rel = '') or period = '') and flag_rel = '') and ttype = '')) ");
        System.out.println(HistoryApplyUtil.format(a));
    }

    @Test
    public void testNotIn() throws Exception {
        FilterDTO a = HistoryApplyUtil.parseSql("( tym not in ( '布地奈德','莫米松','氟替卡松' ) )");
        System.out.println(HistoryApplyUtil.format(a));
    }

    @Test
    public void testNoBracket() throws Exception {
        String sql = "pm in ('阿胶','福字阿胶','复方阿胶浆') and cj ~ '东阿阿胶'";
        System.out.println(HistoryApplyUtil.format(HistoryApplyUtil.parseSql(sql)));
    }


    @Test
    public void testAAAA() throws Exception {
        Object obj = new Object();
        ArrayList<Object> xx = new ArrayList<>();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 50000; i++) {
            xx.add(obj);
        }
        System.out.println(System.currentTimeMillis() - start);

        start = System.currentTimeMillis();
        LinkedList<Object> ll = new LinkedList<>();
        for (int i = 0; i < 50000; i++) {
            ll.add(obj);
        }
        System.out.println(System.currentTimeMillis() - start);
    }

    @Test
    public void testReverse() throws Exception {
//        String sql = "(tym in('二甲双胍维格列汀','西格列汀二甲双胍','利格列汀二甲双胍','沙格列汀二甲双胍','二甲双胍恩格列净' ) )\n \n";
        String sql = "(  prodcode = 'P012' and sort2 = '退热贴' ) or ( prodcode = 'P012' and sort2 = '抗偏头痛药' ) or ( prodcode = 'P012' and sort2 = '痛风/高尿酸血症' ) or ( prodcode = 'P012' and sort3 = '解痉药' ) or ( prodcode = 'P012' and  tym ~  '氨基葡萄糖' ) or ( jx ~  '灌肠剂|喷剂|软膏与乳膏剂|栓剂|贴剂|注射剂' ) \n";
//        String sql = "(    sort3 ='回收站'  ) ";
        String result = convertReverseSql(sql);
        log.info("result={}", result);
    }

    @Test
    public void testOrMultile() throws Exception {
//        String apply = "{\"logicalOperator\":\"\",\"filters\":[{\"id\":\"fr2C1695353230769\",\"logicalOperator\":\"and\",\"filters\":[{\"filterItem\":{\"id\":\"PDPr1695353230769\",\"uniqueId\":null,\"tableId\":null,\"tableAlias\":null,\"fieldId\":null,\"fieldName\":null,\"fieldAlias\":null,\"andOr\":null,\"functionalOperator\":null,\"value\":null,\"otherValue\":null,\"isItself\":2,\"filters\":[{\"id\":\"xRkK1695353230769\",\"logicalOperator\":\"or\",\"filters\":[{\"filterItem\":{\"id\":\"kNEf1695353230769\",\"uniqueId\":null,\"tableId\":null,\"tableAlias\":null,\"fieldId\":null,\"fieldName\":null,\"fieldAlias\":null,\"andOr\":null,\"functionalOperator\":null,\"value\":null,\"otherValue\":null,\"isItself\":2,\"filters\":[{\"id\":\"ZBpm1695353230769\",\"logicalOperator\":\"and\",\"filters\":[{\"id\":\"kaXa1695353230769\",\"logicalOperator\":\"and\",\"filterItem\":{\"id\":null,\"uniqueId\":null,\"tableId\":null,\"tableAlias\":null,\"fieldId\":160,\"fieldName\":\"prodcode\",\"fieldAlias\":null,\"andOr\":\"and\",\"functionalOperator\":\"equalTo\",\"value\":\"P023\",\"otherValue\":null,\"isItself\":2,\"filters\":null,\"timeDimension\":null,\"timeViewName\":null},\"fatherId\":\"kNEf1695353230769\"},{\"id\":\"8eMJ1695353230769\",\"logicalOperator\":\"and\",\"filterItem\":{\"id\":null,\"uniqueId\":null,\"tableId\":null,\"tableAlias\":null,\"fieldId\":112,\"fieldName\":\"sort2\",\"fieldAlias\":null,\"andOr\":\"and\",\"functionalOperator\":\"equalTo\",\"value\":\"咽喉用药\",\"otherValue\":null,\"isItself\":2,\"filters\":null,\"timeDimension\":null,\"timeViewName\":null},\"fatherId\":\"kNEf1695353230769\"}],\"fatherId\":\"kNEf1695353230769\"}],\"timeDimension\":null,\"timeViewName\":null},\"isFather\":1,\"fatherId\":\"xRkK1695353230769\"},{\"filterItem\":{\"id\":\"Npm21695353230769\",\"uniqueId\":null,\"tableId\":null,\"tableAlias\":null,\"fieldId\":null,\"fieldName\":null,\"fieldAlias\":null,\"andOr\":null,\"functionalOperator\":null,\"value\":null,\"otherValue\":null,\"isItself\":2,\"filters\":[{\"id\":\"Tf3f1695353230769\",\"logicalOperator\":\"and\",\"filters\":[{\"id\":\"bxCM1695353230769\",\"logicalOperator\":\"and\",\"filterItem\":{\"id\":null,\"uniqueId\":null,\"tableId\":null,\"tableAlias\":null,\"fieldId\":160,\"fieldName\":\"prodcode\",\"fieldAlias\":null,\"andOr\":\"and\",\"functionalOperator\":\"equalTo\",\"value\":\"P002\",\"otherValue\":null,\"isItself\":2,\"filters\":null,\"timeDimension\":null,\"timeViewName\":null},\"fatherId\":\"Npm21695353230769\"},{\"id\":\"zD471695353230769\",\"logicalOperator\":\"and\",\"filterItem\":{\"id\":null,\"uniqueId\":null,\"tableId\":null,\"tableAlias\":null,\"fieldId\":112,\"fieldName\":\"sort2\",\"fieldAlias\":null,\"andOr\":\"and\",\"functionalOperator\":\"equalTo\",\"value\":\"清热类\",\"otherValue\":null,\"isItself\":2,\"filters\":null,\"timeDimension\":null,\"timeViewName\":null},\"fatherId\":\"Npm21695353230769\"},{\"id\":\"AbK61695353230769\",\"logicalOperator\":\"and\",\"filterItem\":{\"id\":null,\"uniqueId\":null,\"tableId\":null,\"tableAlias\":null,\"fieldId\":232,\"fieldName\":\"pm\",\"fieldAlias\":null,\"andOr\":\"and\",\"functionalOperator\":\"contain\",\"value\":\"二丁颗粒\",\"otherValue\":null,\"isItself\":2,\"filters\":null,\"timeDimension\":null,\"timeViewName\":null},\"fatherId\":\"Npm21695353230769\"},{\"id\":\"BQW21695353230769\",\"logicalOperator\":\"and\",\"filterItem\":{\"id\":null,\"uniqueId\":null,\"tableId\":null,\"tableAlias\":null,\"fieldId\":227,\"fieldName\":\"cj\",\"fieldAlias\":null,\"andOr\":\"and\",\"functionalOperator\":\"contain\",\"value\":\"修正药业集团长春高新\",\"otherValue\":null,\"isItself\":2,\"filters\":null,\"timeDimension\":null,\"timeViewName\":null},\"fatherId\":\"Npm21695353230769\"}],\"fatherId\":\"Npm21695353230769\"}],\"timeDimension\":null,\"timeViewName\":null},\"isFather\":1,\"fatherId\":\"xRkK1695353230769\"}],\"fatherId\":\"PDPr1695353230769\"}],\"timeDimension\":null,\"timeViewName\":null},\"isFather\":1,\"fatherId\":\"fr2C1695353230769\"},{\"filterItem\":{\"id\":\"tH4Z1695353230769\",\"uniqueId\":null,\"tableId\":null,\"tableAlias\":null,\"fieldId\":null,\"fieldName\":null,\"fieldAlias\":null,\"andOr\":null,\"functionalOperator\":null,\"value\":null,\"otherValue\":null,\"isItself\":2,\"filters\":[{\"id\":\"NFkE1695353230769\",\"logicalOperator\":\"or\",\"filters\":[{\"id\":\"FwPQ1695353230769\",\"logicalOperator\":\"or\",\"filterItem\":{\"id\":null,\"uniqueId\":null,\"tableId\":null,\"tableAlias\":null,\"fieldId\":160,\"fieldName\":\"prodcode\",\"fieldAlias\":null,\"andOr\":\"or\",\"functionalOperator\":\"notEqualTo\",\"value\":\"P023\",\"otherValue\":null,\"isItself\":2,\"filters\":null,\"timeDimension\":null,\"timeViewName\":null},\"fatherId\":\"tH4Z1695353230769\"},{\"id\":\"cJXn1695353230769\",\"logicalOperator\":\"or\",\"filterItem\":{\"id\":null,\"uniqueId\":null,\"tableId\":null,\"tableAlias\":null,\"fieldId\":113,\"fieldName\":\"sort3\",\"fieldAlias\":null,\"andOr\":\"or\",\"functionalOperator\":\"notEqualTo\",\"value\":\"回收站\",\"otherValue\":null,\"isItself\":2,\"filters\":null,\"timeDimension\":null,\"timeViewName\":null},\"fatherId\":\"tH4Z1695353230769\"}],\"fatherId\":\"tH4Z1695353230769\"}],\"timeDimension\":null,\"timeViewName\":null},\"isFather\":1,\"fatherId\":\"fr2C1695353230769\"}],\"fatherId\":\"AkNy1695353230769\"}]}";
//        String apply = "{\"logicalOperator\":\"\",\"filters\":[{\"id\":\"fr2C1695353230769\",\"logicalOperator\":\"and\",\"filters\":[{\"filterItem\":{\"id\":\"PDPr1695353230769\",\"uniqueId\":null,\"tableId\":null,\"tableAlias\":\"template\",\"fieldId\":null,\"fieldName\":null,\"fieldAlias\":null,\"andOr\":null,\"functionalOperator\":null,\"value\":null,\"otherValue\":null,\"isItself\":2,\"filters\":[{\"id\":\"xRkK1695353230769\",\"logicalOperator\":\"or\",\"filters\":[{\"filterItem\":{\"id\":\"kNEf1695353230769\",\"uniqueId\":null,\"tableId\":null,\"tableAlias\":\"template\",\"fieldId\":null,\"fieldName\":null,\"fieldAlias\":null,\"andOr\":null,\"functionalOperator\":null,\"value\":null,\"otherValue\":null,\"isItself\":2,\"filters\":[{\"id\":\"ZBpm1695353230769\",\"logicalOperator\":\"and\",\"filters\":[{\"id\":\"kaXa1695353230769\",\"logicalOperator\":\"and\",\"filterItem\":{\"id\":null,\"uniqueId\":null,\"tableId\":null,\"tableAlias\":\"template\",\"fieldId\":160,\"fieldName\":\"t_1_prodcode\",\"fieldAlias\":null,\"andOr\":\"and\",\"functionalOperator\":\"equalTo\",\"value\":\"P023\",\"otherValue\":null,\"isItself\":2,\"filters\":null,\"timeDimension\":null,\"timeViewName\":null},\"fatherId\":\"kNEf1695353230769\"},{\"id\":\"8eMJ1695353230769\",\"logicalOperator\":\"and\",\"filterItem\":{\"id\":null,\"uniqueId\":null,\"tableId\":null,\"tableAlias\":\"template\",\"fieldId\":112,\"fieldName\":\"t_1_sort2\",\"fieldAlias\":null,\"andOr\":\"and\",\"functionalOperator\":\"equalTo\",\"value\":\"咽喉用药\",\"otherValue\":null,\"isItself\":2,\"filters\":null,\"timeDimension\":null,\"timeViewName\":null},\"fatherId\":\"kNEf1695353230769\"}],\"fatherId\":\"kNEf1695353230769\"}],\"timeDimension\":null,\"timeViewName\":null},\"isFather\":1,\"fatherId\":\"xRkK1695353230769\"},{\"filterItem\":{\"id\":\"Npm21695353230769\",\"uniqueId\":null,\"tableId\":null,\"tableAlias\":\"template\",\"fieldId\":null,\"fieldName\":null,\"fieldAlias\":null,\"andOr\":null,\"functionalOperator\":null,\"value\":null,\"otherValue\":null,\"isItself\":2,\"filters\":[{\"id\":\"Tf3f1695353230769\",\"logicalOperator\":\"and\",\"filters\":[{\"id\":\"bxCM1695353230769\",\"logicalOperator\":\"and\",\"filterItem\":{\"id\":null,\"uniqueId\":null,\"tableId\":null,\"tableAlias\":\"template\",\"fieldId\":160,\"fieldName\":\"t_1_prodcode\",\"fieldAlias\":null,\"andOr\":\"and\",\"functionalOperator\":\"equalTo\",\"value\":\"P002\",\"otherValue\":null,\"isItself\":2,\"filters\":null,\"timeDimension\":null,\"timeViewName\":null},\"fatherId\":\"Npm21695353230769\"},{\"id\":\"zD471695353230769\",\"logicalOperator\":\"and\",\"filterItem\":{\"id\":null,\"uniqueId\":null,\"tableId\":null,\"tableAlias\":\"template\",\"fieldId\":112,\"fieldName\":\"t_1_sort2\",\"fieldAlias\":null,\"andOr\":\"and\",\"functionalOperator\":\"equalTo\",\"value\":\"清热类\",\"otherValue\":null,\"isItself\":2,\"filters\":null,\"timeDimension\":null,\"timeViewName\":null},\"fatherId\":\"Npm21695353230769\"},{\"id\":\"AbK61695353230769\",\"logicalOperator\":\"and\",\"filterItem\":{\"id\":null,\"uniqueId\":null,\"tableId\":null,\"tableAlias\":\"template\",\"fieldId\":232,\"fieldName\":\"t_1_pm\",\"fieldAlias\":null,\"andOr\":\"and\",\"functionalOperator\":\"contain\",\"value\":\"二丁颗粒\",\"otherValue\":null,\"isItself\":2,\"filters\":null,\"timeDimension\":null,\"timeViewName\":null},\"fatherId\":\"Npm21695353230769\"},{\"id\":\"BQW21695353230769\",\"logicalOperator\":\"and\",\"filterItem\":{\"id\":null,\"uniqueId\":null,\"tableId\":null,\"tableAlias\":\"template\",\"fieldId\":227,\"fieldName\":\"t_1_cj\",\"fieldAlias\":null,\"andOr\":\"and\",\"functionalOperator\":\"contain\",\"value\":\"修正药业集团长春高新\",\"otherValue\":null,\"isItself\":2,\"filters\":null,\"timeDimension\":null,\"timeViewName\":null},\"fatherId\":\"Npm21695353230769\"}],\"fatherId\":\"Npm21695353230769\"}],\"timeDimension\":null,\"timeViewName\":null},\"isFather\":1,\"fatherId\":\"xRkK1695353230769\"}],\"fatherId\":\"PDPr1695353230769\"}],\"timeDimension\":null,\"timeViewName\":null},\"isFather\":1,\"fatherId\":\"fr2C1695353230769\"},{\"filterItem\":{\"id\":\"tH4Z1695353230769\",\"uniqueId\":null,\"tableId\":null,\"tableAlias\":\"template\",\"fieldId\":null,\"fieldName\":null,\"fieldAlias\":null,\"andOr\":null,\"functionalOperator\":null,\"value\":null,\"otherValue\":null,\"isItself\":2,\"filters\":[{\"id\":\"NFkE1695353230769\",\"logicalOperator\":\"or\",\"filters\":[{\"id\":\"FwPQ1695353230769\",\"logicalOperator\":\"or\",\"filterItem\":{\"id\":null,\"uniqueId\":null,\"tableId\":null,\"tableAlias\":\"template\",\"fieldId\":160,\"fieldName\":\"t_1_prodcode\",\"fieldAlias\":null,\"andOr\":\"or\",\"functionalOperator\":\"notEqualTo\",\"value\":\"P023\",\"otherValue\":null,\"isItself\":2,\"filters\":null,\"timeDimension\":null,\"timeViewName\":null},\"fatherId\":\"tH4Z1695353230769\"},{\"id\":\"cJXn1695353230769\",\"logicalOperator\":\"or\",\"filterItem\":{\"id\":null,\"uniqueId\":null,\"tableId\":null,\"tableAlias\":\"template\",\"fieldId\":113,\"fieldName\":\"t_1_sort3\",\"fieldAlias\":null,\"andOr\":\"or\",\"functionalOperator\":\"notEqualTo\",\"value\":\"回收站\",\"otherValue\":null,\"isItself\":2,\"filters\":null,\"timeDimension\":null,\"timeViewName\":null},\"fatherId\":\"tH4Z1695353230769\"}],\"fatherId\":\"tH4Z1695353230769\"}],\"timeDimension\":null,\"timeViewName\":null},\"isFather\":1,\"fatherId\":\"fr2C1695353230769\"}],\"fatherId\":\"AkNy1695353230769\"}]}";
        String apply = "{\n" +
                "  \"logicalOperator\": \"\",\n" +
                "  \"filters\": [\n" +
                "    {\n" +
                "      \"id\": \"fr2C1695353230769\",\n" +
                "      \"logicalOperator\": \"and\",\n" +
                "      \"filters\": [\n" +
                "        {\n" +
                "          \"filterItem\": {\n" +
                "            \"id\": \"PDPr1695353230769\",\n" +
                "            \"uniqueId\": null,\n" +
                "            \"tableId\": null,\n" +
                "            \"tableAlias\": \"template\",\n" +
                "            \"fieldId\": null,\n" +
                "            \"fieldName\": null,\n" +
                "            \"fieldAlias\": null,\n" +
                "            \"andOr\": null,\n" +
                "            \"functionalOperator\": null,\n" +
                "            \"value\": null,\n" +
                "            \"otherValue\": null,\n" +
                "            \"isItself\": 2,\n" +
                "            \"filters\": [\n" +
                "              {\n" +
                "                \"id\": \"xRkK1695353230769\",\n" +
                "                \"logicalOperator\": \"or\",\n" +
                "                \"filters\": [\n" +
                "                  {\n" +
                "                    \"filterItem\": {\n" +
                "                      \"id\": \"kNEf1695353230769\",\n" +
                "                      \"uniqueId\": null,\n" +
                "                      \"logicalOperator\": \"or\",\n" +
                "                      \"tableId\": null,\n" +
                "                      \"tableAlias\": \"template\",\n" +
                "                      \"fieldId\": null,\n" +
                "                      \"fieldName\": null,\n" +
                "                      \"fieldAlias\": null,\n" +
                "                      \"andOr\": null,\n" +
                "                      \"functionalOperator\": null,\n" +
                "                      \"value\": null,\n" +
                "                      \"otherValue\": null,\n" +
                "                      \"isItself\": 2,\n" +
                "                      \"filters\": [\n" +
                "                        {\n" +
                "                          \"id\": \"ZBpm1695353230769\",\n" +
                "                          \"logicalOperator\": \"and\",\n" +
                "                          \"filters\": [\n" +
                "                            {\n" +
                "                              \"id\": \"kaXa1695353230769\",\n" +
                "                              \"logicalOperator\": \"and\",\n" +
                "                              \"filterItem\": {\n" +
                "                                \"id\": null,\n" +
                "                                \"uniqueId\": null,\n" +
                "                                \"tableId\": null,\n" +
                "                                \"tableAlias\": \"template\",\n" +
                "                                \"fieldId\": 160,\n" +
                "                                \"fieldName\": \"t_1_prodcode\",\n" +
                "                                \"fieldAlias\": null,\n" +
                "                                \"andOr\": \"and\",\n" +
                "                                \"functionalOperator\": \"equalTo\",\n" +
                "                                \"value\": \"P023\",\n" +
                "                                \"otherValue\": null,\n" +
                "                                \"isItself\": 2,\n" +
                "                                \"filters\": null,\n" +
                "                                \"timeDimension\": null,\n" +
                "                                \"timeViewName\": null\n" +
                "                              },\n" +
                "                              \"fatherId\": \"kNEf1695353230769\"\n" +
                "                            },\n" +
                "                            {\n" +
                "                              \"id\": \"8eMJ1695353230769\",\n" +
                "                              \"logicalOperator\": \"and\",\n" +
                "                              \"filterItem\": {\n" +
                "                                \"id\": null,\n" +
                "                                \"uniqueId\": null,\n" +
                "                                \"tableId\": null,\n" +
                "                                \"tableAlias\": \"template\",\n" +
                "                                \"fieldId\": 112,\n" +
                "                                \"fieldName\": \"t_1_sort2\",\n" +
                "                                \"fieldAlias\": null,\n" +
                "                                \"andOr\": \"and\",\n" +
                "                                \"functionalOperator\": \"equalTo\",\n" +
                "                                \"value\": \"咽喉用药\",\n" +
                "                                \"otherValue\": null,\n" +
                "                                \"isItself\": 2,\n" +
                "                                \"filters\": null,\n" +
                "                                \"timeDimension\": null,\n" +
                "                                \"timeViewName\": null\n" +
                "                              },\n" +
                "                              \"fatherId\": \"kNEf1695353230769\"\n" +
                "                            }\n" +
                "                          ],\n" +
                "                          \"fatherId\": \"kNEf1695353230769\"\n" +
                "                        }\n" +
                "                      ],\n" +
                "                      \"timeDimension\": null,\n" +
                "                      \"timeViewName\": null\n" +
                "                    },\n" +
                "                    \"isFather\": 1,\n" +
                "                    \"fatherId\": \"xRkK1695353230769\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"filterItem\": {\n" +
                "                      \"id\": \"Npm21695353230769\",\n" +
                "                      \"uniqueId\": null,\n" +
                "                      \"tableId\": null,\n" +
                "                      \"tableAlias\": \"template\",\n" +
                "                      \"fieldId\": null,\n" +
                "                      \"fieldName\": null,\n" +
                "                      \"fieldAlias\": null,\n" +
                "                      \"andOr\": null,\n" +
                "                      \"functionalOperator\": null,\n" +
                "                      \"value\": null,\n" +
                "                      \"otherValue\": null,\n" +
                "                      \"isItself\": 2,\n" +
                "                      \"filters\": [\n" +
                "                        {\n" +
                "                          \"id\": \"Tf3f1695353230769\",\n" +
                "                          \"logicalOperator\": \"and\",\n" +
                "                          \"filters\": [\n" +
                "                            {\n" +
                "                              \"id\": \"bxCM1695353230769\",\n" +
                "                              \"logicalOperator\": \"and\",\n" +
                "                              \"filterItem\": {\n" +
                "                                \"id\": null,\n" +
                "                                \"uniqueId\": null,\n" +
                "                                \"tableId\": null,\n" +
                "                                \"tableAlias\": \"template\",\n" +
                "                                \"fieldId\": 160,\n" +
                "                                \"fieldName\": \"t_1_prodcode\",\n" +
                "                                \"fieldAlias\": null,\n" +
                "                                \"andOr\": \"and\",\n" +
                "                                \"functionalOperator\": \"equalTo\",\n" +
                "                                \"value\": \"P002\",\n" +
                "                                \"otherValue\": null,\n" +
                "                                \"isItself\": 2,\n" +
                "                                \"filters\": null,\n" +
                "                                \"timeDimension\": null,\n" +
                "                                \"timeViewName\": null\n" +
                "                              },\n" +
                "                              \"fatherId\": \"Npm21695353230769\"\n" +
                "                            },\n" +
                "                            {\n" +
                "                              \"id\": \"zD471695353230769\",\n" +
                "                              \"logicalOperator\": \"and\",\n" +
                "                              \"filterItem\": {\n" +
                "                                \"id\": null,\n" +
                "                                \"uniqueId\": null,\n" +
                "                                \"tableId\": null,\n" +
                "                                \"tableAlias\": \"template\",\n" +
                "                                \"fieldId\": 112,\n" +
                "                                \"fieldName\": \"t_1_sort2\",\n" +
                "                                \"fieldAlias\": null,\n" +
                "                                \"andOr\": \"and\",\n" +
                "                                \"functionalOperator\": \"equalTo\",\n" +
                "                                \"value\": \"清热类\",\n" +
                "                                \"otherValue\": null,\n" +
                "                                \"isItself\": 2,\n" +
                "                                \"filters\": null,\n" +
                "                                \"timeDimension\": null,\n" +
                "                                \"timeViewName\": null\n" +
                "                              },\n" +
                "                              \"fatherId\": \"Npm21695353230769\"\n" +
                "                            },\n" +
                "                            {\n" +
                "                              \"id\": \"AbK61695353230769\",\n" +
                "                              \"logicalOperator\": \"and\",\n" +
                "                              \"filterItem\": {\n" +
                "                                \"id\": null,\n" +
                "                                \"uniqueId\": null,\n" +
                "                                \"tableId\": null,\n" +
                "                                \"tableAlias\": \"template\",\n" +
                "                                \"fieldId\": 232,\n" +
                "                                \"fieldName\": \"t_1_pm\",\n" +
                "                                \"fieldAlias\": null,\n" +
                "                                \"andOr\": \"and\",\n" +
                "                                \"functionalOperator\": \"contain\",\n" +
                "                                \"value\": \"二丁颗粒\",\n" +
                "                                \"otherValue\": null,\n" +
                "                                \"isItself\": 2,\n" +
                "                                \"filters\": null,\n" +
                "                                \"timeDimension\": null,\n" +
                "                                \"timeViewName\": null\n" +
                "                              },\n" +
                "                              \"fatherId\": \"Npm21695353230769\"\n" +
                "                            },\n" +
                "                            {\n" +
                "                              \"id\": \"BQW21695353230769\",\n" +
                "                              \"logicalOperator\": \"and\",\n" +
                "                              \"filterItem\": {\n" +
                "                                \"id\": null,\n" +
                "                                \"uniqueId\": null,\n" +
                "                                \"tableId\": null,\n" +
                "                                \"tableAlias\": \"template\",\n" +
                "                                \"fieldId\": 227,\n" +
                "                                \"fieldName\": \"t_1_cj\",\n" +
                "                                \"fieldAlias\": null,\n" +
                "                                \"andOr\": \"and\",\n" +
                "                                \"functionalOperator\": \"contain\",\n" +
                "                                \"value\": \"修正药业集团长春高新\",\n" +
                "                                \"otherValue\": null,\n" +
                "                                \"isItself\": 2,\n" +
                "                                \"filters\": null,\n" +
                "                                \"timeDimension\": null,\n" +
                "                                \"timeViewName\": null\n" +
                "                              },\n" +
                "                              \"fatherId\": \"Npm21695353230769\"\n" +
                "                            }\n" +
                "                          ],\n" +
                "                          \"fatherId\": \"Npm21695353230769\"\n" +
                "                        }\n" +
                "                      ],\n" +
                "                      \"timeDimension\": null,\n" +
                "                      \"timeViewName\": null\n" +
                "                    },\n" +
                "                    \"isFather\": 1,\n" +
                "                    \"fatherId\": \"xRkK1695353230769\"\n" +
                "                  }\n" +
                "                ],\n" +
                "                \"fatherId\": \"PDPr1695353230769\"\n" +
                "              }\n" +
                "            ],\n" +
                "            \"timeDimension\": null,\n" +
                "            \"timeViewName\": null\n" +
                "          },\n" +
                "          \"isFather\": 1,\n" +
                "          \"fatherId\": \"fr2C1695353230769\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"filterItem\": {\n" +
                "            \"id\": \"tH4Z1695353230769\",\n" +
                "            \"uniqueId\": null,\n" +
                "            \"tableId\": null,\n" +
                "            \"tableAlias\": \"template\",\n" +
                "            \"fieldId\": null,\n" +
                "            \"fieldName\": null,\n" +
                "            \"fieldAlias\": null,\n" +
                "            \"andOr\": null,\n" +
                "            \"functionalOperator\": null,\n" +
                "            \"value\": null,\n" +
                "            \"otherValue\": null,\n" +
                "            \"isItself\": 2,\n" +
                "            \"filters\": [\n" +
                "              {\n" +
                "                \"id\": \"NFkE1695353230769\",\n" +
                "                \"logicalOperator\": \"or\",\n" +
                "                \"filters\": [\n" +
                "                  {\n" +
                "                    \"id\": \"FwPQ1695353230769\",\n" +
                "                    \"logicalOperator\": \"or\",\n" +
                "                    \"filterItem\": {\n" +
                "                      \"id\": null,\n" +
                "                      \"uniqueId\": null,\n" +
                "                      \"tableId\": null,\n" +
                "                      \"tableAlias\": \"template\",\n" +
                "                      \"fieldId\": 160,\n" +
                "                      \"fieldName\": \"t_1_prodcode\",\n" +
                "                      \"fieldAlias\": null,\n" +
                "                      \"andOr\": \"or\",\n" +
                "                      \"functionalOperator\": \"notEqualTo\",\n" +
                "                      \"value\": \"P023\",\n" +
                "                      \"otherValue\": null,\n" +
                "                      \"isItself\": 2,\n" +
                "                      \"filters\": null,\n" +
                "                      \"timeDimension\": null,\n" +
                "                      \"timeViewName\": null\n" +
                "                    },\n" +
                "                    \"fatherId\": \"tH4Z1695353230769\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"id\": \"cJXn1695353230769\",\n" +
                "                    \"logicalOperator\": \"or\",\n" +
                "                    \"filterItem\": {\n" +
                "                      \"id\": null,\n" +
                "                      \"uniqueId\": null,\n" +
                "                      \"tableId\": null,\n" +
                "                      \"tableAlias\": \"template\",\n" +
                "                      \"fieldId\": 113,\n" +
                "                      \"fieldName\": \"t_1_sort3\",\n" +
                "                      \"fieldAlias\": null,\n" +
                "                      \"andOr\": \"or\",\n" +
                "                      \"functionalOperator\": \"notEqualTo\",\n" +
                "                      \"value\": \"回收站\",\n" +
                "                      \"otherValue\": null,\n" +
                "                      \"isItself\": 2,\n" +
                "                      \"filters\": null,\n" +
                "                      \"timeDimension\": null,\n" +
                "                      \"timeViewName\": null\n" +
                "                    },\n" +
                "                    \"fatherId\": \"tH4Z1695353230769\"\n" +
                "                  }\n" +
                "                ],\n" +
                "                \"fatherId\": \"tH4Z1695353230769\"\n" +
                "              }\n" +
                "            ],\n" +
                "            \"timeDimension\": null,\n" +
                "            \"timeViewName\": null\n" +
                "          },\n" +
                "          \"isFather\": 1,\n" +
                "          \"fatherId\": \"fr2C1695353230769\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"fatherId\": \"AkNy1695353230769\"\n" +
                "    }\n" +
                "  ]\n" +
                "}\n";
        System.out.println(apply);
        FilterDTO filterDTO = JsonUtils.parse(apply, FilterDTO.class);
        HistoryApplyUtil.buildSql(filterDTO);
    }



    @Test
    public void testJudgeNoProjectRelation() throws Exception {
        List<String> main = Files.readAllLines(Paths.get("/home/zk/Note/WorkLog/ZK/tg-easy/transfer-old/project.log"));
        List<String> applys = Files.readAllLines(Paths.get("/home/zk/Note/WorkLog/ZK/tg-easy/transfer-old/apply.log"));
        List<String> flows = Files.readAllLines(Paths.get("/home/zk/Note/WorkLog/ZK/tg-easy/transfer-old/flow.log"));
        applys.removeAll(main);
        flows.removeAll(main);

        log.info("applys={}", new HashSet<>(applys));
        log.info("flows={}", new HashSet<>(flows));
    }
}


