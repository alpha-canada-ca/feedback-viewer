package ca.gc.tbs.service;

import ca.gc.tbs.domain.Role;
import ca.gc.tbs.domain.User;

import ca.gc.tbs.repository.RoleRepository;
import ca.gc.tbs.repository.UserRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

	public static final String USER_ROLE = "USER";

	public static final String ADMIN_ROLE = "ADMIN";

	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	public User findUserByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	public User findUserById(String Id) {
		return userRepository.findById(Id).get();
	}

	public List<User> findUserByRole(String role) {
		Role oRole = this.roleRepository.findByRole(role);
		return userRepository.findByRolesContaining(oRole);
	}

	public void deleteUserById(String Id) {
		userRepository.deleteById(Id);
	}

	public List<User> findAllUsers() {
		return userRepository.findAll();
	}

	public List<String> findInstitutions() {
		return userRepository.findAllInstitutions();
	}

	public User getCurrentUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String username = ((org.springframework.security.core.userdetails.User) auth.getPrincipal()).getUsername();
		return this.findUserByEmail(username);
	}

	public void saveUser(User user) {
		user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
		user.setDateCreated(DATE_FORMAT.format(new Date()));
		Role userRole = null;
		if (this.userRepository.count() <= 0) {
			user.setEnabled(true);
			userRole = roleRepository.findByRole(ADMIN_ROLE);
		} else {
			userRole = roleRepository.findByRole(USER_ROLE);
		}
		user.setRoles(new HashSet<>(Arrays.asList(userRole)));
		userRepository.save(user);
	}
	
	public boolean isAdmin(User user) {
		
		for (Role role : user.getRoles()) {
			if (role.getRole().contentEquals(ADMIN_ROLE)) {
				return true;
			}
		}
		return false;
	}

	public void enable(String id) {
		User user = this.findUserById(id);
		user.setEnabled(true);
		userRepository.save(user);
	}

	public void enableAdmin(String email) {
		User user = this.findUserByEmail(email);
		user.setRoles(new HashSet<>(Arrays.asList(roleRepository.findByRole(ADMIN_ROLE))));
		user.setEnabled(true);
		userRepository.save(user);
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

		User user = userRepository.findByEmail(email);
		if (user != null && user.isEnabled()) {
			List<GrantedAuthority> authorities = getUserAuthority(user.getRoles());
			return buildUserForAuthentication(user, authorities);
		} else {
			throw new UsernameNotFoundException("username not found");
		}
	}

	private List<GrantedAuthority> getUserAuthority(Set<Role> userRoles) {
		Set<GrantedAuthority> roles = new HashSet<>();
		userRoles.forEach((role) -> {
			roles.add(new SimpleGrantedAuthority(role.getRole()));
		});

		List<GrantedAuthority> grantedAuthorities = new ArrayList<>(roles);
		return grantedAuthorities;
	}

	private UserDetails buildUserForAuthentication(User user, List<GrantedAuthority> authorities) {
		return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), authorities);
	}

}
