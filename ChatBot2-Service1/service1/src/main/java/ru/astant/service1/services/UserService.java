package ru.astant.service1.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.astant.service1.models.User;
import ru.astant.service1.repositories.UserRepository;
import ru.astant.service1.security.CustomUserDetails;

import java.security.Principal;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        Optional<User> user = userRepository.findByUsername(username);
        if(user.isEmpty()){
            throw new UsernameNotFoundException("User not found");
        }
        return new CustomUserDetails(user.get());
    }

    public User getCurrentUser(Principal principal) {
        if(principal == null) {
            return null;
        }
        String username = principal.getName();
        Optional<User> person = userRepository.findByUsername(username);
        if(person.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }
        return person.get();
    }
}
