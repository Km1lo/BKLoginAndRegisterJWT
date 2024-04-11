package pe.edu.upc.loginregisterjwt.servicesinterfaces;

import pe.edu.upc.loginregisterjwt.entities.Users;


import java.util.List;

public interface IUsersService {
    public void insert(Users users);
    public List<Users> listar();
    public void delete(Long id);
    public Users listarId(Long id);
    public Users findByUsername(String username);


}
