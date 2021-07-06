package taxi.mate.service;

import mate.lib.exception.MyAuthenticationException;
import mate.model.Driver;

public interface AuthenticationService {
    Driver login(String login, String password) throws MyAuthenticationException;
}
