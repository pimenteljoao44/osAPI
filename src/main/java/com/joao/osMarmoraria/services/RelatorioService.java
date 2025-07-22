package com.joao.osMarmoraria.services;

import net.sf.jasperreports.engine.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Service
public class RelatorioService {

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private DataSource dataSource;

    public byte[] gerarRelatorioDeVendasResumido(Map<String, Object> parametros) throws Exception {
        JasperReport jasperReport = JasperCompileManager.compileReport(
                resourceLoader.getResource("classpath:relatorios/venda/relatorioDeVendasResumido.jrxml").getInputStream()
        );

        try (var connection = DataSourceUtils.getConnection(dataSource)) {
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parametros, connection);

            return JasperExportManager.exportReportToPdf(jasperPrint);
        }
    }

    public byte[] gerarRelatorioDeComprasResumido(Map<String, Object> parametros) throws Exception {
        JasperReport jasperReport = JasperCompileManager.compileReport(
                resourceLoader.getResource("classpath:relatorios/compra/relatorioDeComprasResumido.jrxml").getInputStream()
        );

        try(var connection = DataSourceUtils.getConnection(dataSource)) {
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport,parametros,connection);

            return JasperExportManager.exportReportToPdf(jasperPrint);
        }
    }
}
