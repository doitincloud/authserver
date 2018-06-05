package doitincloud.oauth2.repositories;

import doitincloud.oauth2.models.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo {

    User findByUsername(String username);

    void delete(String username);
}
