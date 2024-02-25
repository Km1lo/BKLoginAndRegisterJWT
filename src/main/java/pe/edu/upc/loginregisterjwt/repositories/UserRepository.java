package pe.edu.upc.loginregisterjwt.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upc.loginregisterjwt.entities.Users;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {

    public Users findByUsername(String username);
    @Query("select count(u.username) from Users u where u.username =:username")
    public int buscarUsername(@Param("username") String nombre);
}
