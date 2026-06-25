package com.javanauta.usuario.buisness;

import com.javanauta.usuario.buisness.converter.UsuarioConverter;
import com.javanauta.usuario.buisness.dto.UsuarioDTO;
import com.javanauta.usuario.infrastructure.entity.Usuario;
import com.javanauta.usuario.infrastructure.exceptions.ConflictException;
import com.javanauta.usuario.infrastructure.exceptions.ResourceNotFoundException;
import com.javanauta.usuario.infrastructure.repository.UsuarioRepository;
import com.javanauta.usuario.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioConverter usuarioConverter;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


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

    /*
    O metodo abaixo possui a responsabilidade de atualizaro os dados do usuario da seguinte forma:
        - Recebe o dto e verifica os dados passados nele pelo usuario.
        - O dado recebido no dto será atualizado, o dado que nao for passado, ele pega da entity,
            dessa forma, atualizamos o que foi enviado, e mantemos os outros dados.
     */
    public UsuarioDTO atualizarDadosUsuario(String token, UsuarioDTO dto){
        //aqui chamaos o metodo extractUsername() para extrairmos o email do token
        String email = jwtUtil.extractUsername(token.substring(7));

        // Criptografa a senha novamente caso passada no dto, se nao passada, mantem como esta no banco
        dto.setSenha(dto.getSenha() != null ? passwordEncoder.encode(dto.getSenha()) : null);

        Usuario usuarioEntity = usuarioRepository.findByEmail(email)
                .orElseThrow(()-> new ResourceNotFoundException("Email não encontrado"));

        //Este trecho tem a responsabilidade de mesclar os dados passados no dto com os dados da entity
        Usuario usuario = usuarioConverter.updateUsuario(dto, usuarioEntity);


        //Aqui salvamos o usuario no banco e convertemos para dto
        return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));
    }

}
