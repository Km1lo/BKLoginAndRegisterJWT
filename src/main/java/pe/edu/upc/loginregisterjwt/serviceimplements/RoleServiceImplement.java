package pe.edu.upc.loginregisterjwt.serviceimplements;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.edu.upc.loginregisterjwt.entities.Role;
import pe.edu.upc.loginregisterjwt.repositories.RoleRepository;
import pe.edu.upc.loginregisterjwt.servicesinterfaces.IRoleService;

import java.util.List;

@Service

public class RoleServiceImplement implements IRoleService {
    @Autowired
    public RoleRepository rR;
    @Override
    public List<Role> listar() {
        return rR.findAll();
    }

    @Override
    public Role listarId(long idRole) {
        return rR.findById(idRole).orElse(new Role());
    }

    @Override
    public void insRol(String authority, Long userid) {
        rR.insRol(authority,userid);
    }

    @Override
    public void updRol(String authority, Long userid) {
        rR.updRol(authority,userid);
    }

    @Override
    public void delRol(Long userid) {
        rR.delRol(userid);
    }

    @Override
    public void insRolLog() {
        rR.insRolLog();
    }
}
