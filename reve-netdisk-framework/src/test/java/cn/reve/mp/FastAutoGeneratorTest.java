package cn.reve.mp;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.TemplateType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.Collections;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/3/29 15:56
 */
public class FastAutoGeneratorTest {
    public static void main(String[] args) {
        String url = "jdbc:mysql://127.0.0.1:3306/ly_netdisk?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai";
        FastAutoGenerator.create(url, "root", "ly2lym1314")
                .globalConfig(builder -> {
                    builder.author("Rêve") // 设置作者
                            //.enableSwagger() // 开启 swagger 模式
                            .fileOverride() // 覆盖已生成文件
                            .outputDir("E:\\Java\\Project_Code\\springboot_project\\ReveNetDisk\\reve-netdisk-framework\\src\\main\\java"); // 指定输出目录
                    //
                })
                .templateConfig(builder ->{
                    //Controller通过EasyCode进行生成
                    builder.disable(TemplateType.CONTROLLER);
                })
                .packageConfig(builder -> {
                    builder.parent("cn") // 设置父包名
                            //app manager
                            .moduleName("ly") // 设置父包模块名
                            .moduleName("netdisk")
                            .entity("domain.entity")
                            .pathInfo(Collections.singletonMap(OutputFile.mapperXml
                                    , "E:\\Java\\Project_Code\\springboot_project\\ReveNetDisk\\reve-netdisk-framework\\src\\main\\resources\\mapper")); // 设置mapperXml生成路径
                })
                .strategyConfig(builder -> {// 设置需要生成的表名
                    builder.addInclude("user_info","file_info","share_info")
                            // .addTablePrefix("sys")superClass(BaseEntity.class)
                            .addTableSuffix("info")
                            .entityBuilder().enableLombok().enableChainModel()
                            .controllerBuilder().enableRestStyle()
                            .serviceBuilder().formatServiceFileName("%sService")
                            .mapperBuilder().enableBaseResultMap();
                })
                .templateEngine(new FreemarkerTemplateEngine()) // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                .execute();
    }

}
