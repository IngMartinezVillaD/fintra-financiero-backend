package co.pluto.services.impl;

import co.pluto.models.repositories.IUsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

  private final IUsuarioRepository usuarioRepository;

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return usuarioRepository
        .findByUsernameAndDeletedAtIsNull(username)
        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
  }
}
