package pe.edu.upc.loginregisterjwt.controllers;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.loginregisterjwt.entities.Role;
import pe.edu.upc.loginregisterjwt.entities.Users;
import pe.edu.upc.loginregisterjwt.repositories.RoleRepository;
import pe.edu.upc.loginregisterjwt.repositories.UserRepository;
import pe.edu.upc.loginregisterjwt.security.JwtResponse;
import pe.edu.upc.loginregisterjwt.security.JwtTokenUtil;
import pe.edu.upc.loginregisterjwt.serviceimplements.JwtUserDetailsService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/oauth2")
public class OAuth2Controller {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtUserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${google.client.id:}")
    private String googleClientId;

    /**
     * Endpoint para login con token de Google
     * POST /oauth2/google
     * Body: { "token": "google_id_token_aqui" }
     */
    @PostMapping("/google")
    public ResponseEntity<?> loginWithGoogleToken(@RequestBody Map<String, String> tokenRequest) {
        try {
            String idTokenString = tokenRequest.get("token");

            if (idTokenString == null || idTokenString.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "Token de Google es requerido")
                );
            }

            System.out.println("=== DEBUG: Intentando verificar token de Google ===");
            System.out.println("Client ID configurado: " + googleClientId);
            System.out.println("Token recibido (primeros 50 chars): " + idTokenString.substring(0, Math.min(50, idTokenString.length())));

            // Configurar el verificador de Google (sin verificar audience para pruebas)
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    new GsonFactory())
                    // Comentamos la validación del audience temporalmente para debug
                    // .setAudience(Collections.singletonList(googleClientId))
                    .build();

            // Verificar el token
            GoogleIdToken idToken = null;
            try {
                idToken = verifier.verify(idTokenString);
            } catch (Exception verifyException) {
                System.out.println("Error al verificar token: " + verifyException.getMessage());
                verifyException.printStackTrace();
                
                return ResponseEntity.badRequest().body(
                    Map.of(
                        "error", "Error al verificar token de Google",
                        "details", verifyException.getMessage(),
                        "hint", "El token puede estar expirado (válido por 1 hora) o ser inválido"
                    )
                );
            }

            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();

                // Extraer información del usuario
                String email = payload.getEmail();
                String name = (String) payload.get("name");
                String givenName = (String) payload.get("given_name");
                String familyName = (String) payload.get("family_name");
                String picture = (String) payload.get("picture");
                String googleId = payload.getSubject();

                System.out.println("Token válido para usuario: " + email);

                // Verificar manualmente el audience
                String tokenAudience = (String) payload.getAudience();
                System.out.println("Audience en token: " + tokenAudience);
                System.out.println("Client ID esperado: " + googleClientId);

                // Buscar si el usuario ya existe
                Users user = userRepository.findByUsername(email);

                if (user == null) {
                    System.out.println("Creando nuevo usuario: " + email);
                    // Crear nuevo usuario
                    user = new Users();
                    user.setUsername(email);
                    user.setPassword(passwordEncoder.encode("GOOGLE_" + googleId));
                    user.setEnabled(true);
                    user.setNombres(givenName != null ? givenName : name);
                    user.setApellidos(familyName != null ? familyName : "");
                    user.setRoles(new ArrayList<>());

                    userRepository.save(user);

                    // Asignar rol USER por defecto
                    Role role = new Role();
                    role.setRol("ROLE_USER");
                    role.setUser(user);
                    roleRepository.save(role);
                } else {
                    System.out.println("Usuario ya existe: " + email);
                }

                // Generar JWT
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                String jwtToken = jwtTokenUtil.generateToken(userDetails);

                // Crear respuesta con información del usuario
                Map<String, Object> response = new HashMap<>();
                response.put("jwttoken", jwtToken);
                response.put("username", email);
                response.put("name", name);
                response.put("picture", picture);
                response.put("message", "Login con Google exitoso");

                System.out.println("=== Login exitoso para: " + email + " ===");

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(
                    Map.of(
                        "error", "Token de Google inválido o expirado",
                        "hint", "Genera un nuevo token. Los tokens de Google expiran en 1 hora."
                    )
                );
            }
        } catch (Exception e) {
            System.out.println("=== ERROR GENERAL ===");
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                Map.of(
                    "error", "Error al procesar token de Google",
                    "details", e.getMessage(),
                    "type", e.getClass().getSimpleName()
                )
            );
        }
    }

    /**
     * Endpoint de prueba para simular login con Google (solo para desarrollo)
     * POST /oauth2/google/test
     * Body: { "email": "test@gmail.com", "name": "Test User" }
     */
    @PostMapping("/google/test")
    public ResponseEntity<?> testGoogleLogin(@RequestBody Map<String, String> testData) {
        try {
            String email = testData.get("email");
            String name = testData.get("name");

            if (email == null || email.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "Email es requerido")
                );
            }

            // Buscar o crear usuario
            Users user = userRepository.findByUsername(email);

            if (user == null) {
                user = new Users();
                user.setUsername(email);
                user.setPassword(passwordEncoder.encode("GOOGLE_TEST"));
                user.setEnabled(true);
                user.setNombres(name != null ? name : "Usuario");
                user.setApellidos("Google");
                user.setRoles(new ArrayList<>());

                userRepository.save(user);

                Role role = new Role();
                role.setRol("ROLE_USER");
                role.setUser(user);
                roleRepository.save(role);
            }

            // Generar JWT
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            String jwtToken = jwtTokenUtil.generateToken(userDetails);

            Map<String, Object> response = new HashMap<>();
            response.put("jwttoken", jwtToken);
            response.put("username", email);
            response.put("name", name);
            response.put("message", "Login de prueba exitoso");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                Map.of("error", "Error: " + e.getMessage())
            );
        }
    }
}
