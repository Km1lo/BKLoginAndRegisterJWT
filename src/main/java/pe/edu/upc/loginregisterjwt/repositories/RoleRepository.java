package pe.edu.upc.loginregisterjwt.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.loginregisterjwt.entities.Role;


@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    @Transactional
    @Modifying
    @Query(value = "insert into roles (rol, userid) VALUES (:rol, :userid)", nativeQuery = true)
    public void insRol(@Param("rol") String authority, @Param("userid") Long userid);

    @Transactional
    @Modifying
    @Query(value = "UPDATE roles set rol=:rol where userid= :userid", nativeQuery = true)
    public void updRol(@Param("rol") String authority, @Param("userid") Long userid);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM roles WHERE userid = :userid", nativeQuery = true)
    public void delRol(@Param("userid") Long userid);

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO roles (rol, userid)\n" +
            " SELECT 'USER', id FROM users ORDER BY id DESC LIMIT 1;", nativeQuery = true)
    public void insRolLog();
}
