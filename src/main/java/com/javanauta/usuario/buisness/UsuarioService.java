package com.javanauta.usuario.buisness;

import com.javanauta.usuario.buisness.converter.UsuarioConverter;
import com.javanauta.usuario.buisness.dto.UsuarioDTO;
import com.javanauta.usuario.infrastructure.entity.Usuario;
import com.javanauta.usuario.infrastructure.exceptions.ConflictException;
import com.javanauta.usuario.infrastructure.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioConverter usuarioConverter;
    private final PasswordEncoder passwordEncoder;


    // Salva Usuario
    public UsuarioDTO salvaUsuario(UsuarioDTO usuarioDTO){
        emailExiste(usuarioDTO.getEmail());
        usuarioDTO.setSenha(passwordEncoder.encode(usuarioDTO.getSenha()));
        Usuario usuario = usuarioConverter.paraUsuario(usuarioDTO);
        usuario = usuarioRepository.save(usuario);
        return usuarioConverter.paraUsuarioDTO(usuario);
    }

    public void emailExiste(String email){
        try{
            boolean existe = verificaEmailExistente(email);
            if (existe){
                throw new ConflictException("Email já cadastrado" + email);
            }
        } catch (ConflictException e){
            throw new ConflictException("Email já cadastrado", e.getCause());
        }
    }

    /*
    chama metodo criado no repository.
    exclusivo apenas por verificar se um email já existe
    pode ser utilizado/chamado em outro metodo, sendo assim
    não é interessante que o mesmo tenha uma regra de negocio dentro dele
    interessante que apenas retorne o valor, e que o metodo que o chamos
    se responsabilize por realizar a regra de negocio
     */
    public boolean verificaEmailExistente(String email){
        return usuarioRepository.existsByEmail(email);
    }

    public Usuario buscarUsuarioPorEmail(String usuario){
        return usuarioRepository.findByEmail(usuario)
                .orElseThrow(()-> new ConflictException("Usuario não encontrado"));
    }

    public void deleteUsuarioPorEmial(String email){
        usuarioRepository.deleteByEmail(email);
    }
}
