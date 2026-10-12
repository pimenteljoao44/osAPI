package com.joao.osMarmoraria.services;

import lombok.RequiredArgsConstructor;

import net.sf.jasperreports.engine.*;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final ResourceLoader resourceLoader;

    private final DataSource dataSource;

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
