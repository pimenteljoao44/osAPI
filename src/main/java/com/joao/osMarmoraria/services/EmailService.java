package com.joao.osMarmoraria.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envio de e-mails transacionais.
 *
 * <p>A dependencia {@link JavaMailSender} eh opcional: quando {@code spring.mail.host}
 * nao esta configurado, o Spring Boot nao cria o bean automaticamente. Usamos
 * {@link ObjectProvider} para que a aplicacao possa subir mesmo sem SMTP (util em
 * homolog/testes), e o envio falha de forma controlada apenas quando efetivamente
 * chamado.</p>
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${spring.mail.username:no-reply@localhost}")
    private String remetente;

    public EmailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    public String enviarEmailTexto(String destinatario, String titulo, String mensagem) {
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) {
            log.warn("JavaMailSender nao configurado. Email para {} nao enviado (titulo: {}). "
                    + "Defina spring.mail.host para habilitar envio real.", destinatario, titulo);
            return "Email nao enviado: SMTP nao configurado.";
        }
        try {
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setFrom(remetente);
            simpleMailMessage.setTo(destinatario);
            simpleMailMessage.setSubject(titulo);
            simpleMailMessage.setText(mensagem);
            sender.send(simpleMailMessage);
            return "Email enviado!";
        } catch (Exception e) {
            log.error("Falha ao enviar email para {}: {}", destinatario, e.getMessage(), e);
            return "Erro ao enviar email.";
        }
    }
}
