package com.joao.osMarmoraria.controle;

import com.joao.osMarmoraria.domain.Usuario;
import com.joao.osMarmoraria.dtos.*;
import com.joao.osMarmoraria.repository.UsuarioRepository;
import com.joao.osMarmoraria.services.EmailService;
import com.joao.osMarmoraria.services.RandomPasswordGeneratorService;
import com.joao.osMarmoraria.services.TokenService;
import com.joao.osMarmoraria.services.UsuarioService;
import com.joao.osMarmoraria.services.exceptions.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@CrossOrigin("*")
@RestController
@RequestMapping("auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private RandomPasswordGeneratorService randomPasswordGeneratorService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }
    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid AuthenticationDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.login(), data.senha());
        var auth = this.authenticationManager.authenticate(usernamePassword);
        Usuario usuario = repository.findByLogin(data.login());
        if (usuario != null && new BCryptPasswordEncoder().matches(data.senha(), usuario.getSenha())) {
            var token = tokenService.generateToken((Usuario) auth.getPrincipal());
            return ResponseEntity.ok(new LoginResponseDTO(
                    usuario.getId(), usuario.getNome(),usuario.getLogin(),usuario.getSenha(),usuario.getNivelAcesso(),token
            ));
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/recovery")
    public ResponseEntity recovery(@RequestBody @Valid RecoveryDTO data) {
        var usuario = repository.findByEmail(data.getEmail());
        if (usuario != null) {
            String senhaTemp = randomPasswordGeneratorService.generateRandomPassword();

            usuarioService.update(new UsuarioDTO(usuario.getId(),usuario.getNome(),usuario.getLogin(),senhaTemp,usuario.getEmail(),usuario.getNivelAcesso().getCod()));
            emailService.enviarEmailTexto(usuario.getEmail(), "Recuperção de Senha", "Olá "+
                    usuario.getNome() + " Sua senha de acesso é: " + senhaTemp);

            return ResponseEntity.ok(new RecoveryDTO(usuario.getEmail(),senhaTemp));
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

}
