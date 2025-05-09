package org.zerox80.coingeckowebapp.repository; // ✅ Korrekter Paketname (statt "repostiroy")

import org.springframework.data.jpa.repository.JpaRepository;
import org.zerox80.coingeckowebapp.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}