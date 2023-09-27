package io.jenkins.plugins.twofactor.jenkins;

import static io.jenkins.plugins.twofactor.constants.MoGlobalConfigConstant.UtilityGlobalConstants.SESSION_2FA_VERIFICATION;

import hudson.Extension;
import hudson.model.User;
import hudson.security.csrf.CrumbExclusion;
import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Extension
@SuppressWarnings("unused")
public class MoTfaCrumbExclusion extends CrumbExclusion {
  private static final Logger LOGGER = Logger.getLogger(MoTfaCrumbExclusion.class.getName());

  @Override
  public boolean process(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {
    HttpSession session = request.getSession();
    User user = User.current();

    if (user == null || session == null || ignoreCrumbCheck(session, user.getId())) {
      filterChain.doFilter(request, response);
      return true;
    }
    return false;
  }

  private boolean ignoreCrumbCheck(HttpSession session, String userId) {
    String sessionAttributeKey = userId + SESSION_2FA_VERIFICATION.getKey();
    Object tfaVerificationAttribute = session.getAttribute(sessionAttributeKey);

    if (tfaVerificationAttribute == null) {
      return true;
    }

    return !Boolean.parseBoolean(tfaVerificationAttribute.toString());
  }
}
