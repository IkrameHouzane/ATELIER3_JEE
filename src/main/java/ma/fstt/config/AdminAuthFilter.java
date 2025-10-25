package ma.fstt.config;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ma.fstt.beans.AuthBean;

import java.io.IOException;

@WebFilter(filterName = "AdminAuthFilter", urlPatterns = {"/admin-produits.xhtml", "/categories.xhtml", "/admin-commandes.xhtml"})
public class AdminAuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // Get AuthBean from session
        AuthBean authBean = (AuthBean) req.getSession().getAttribute("authBean");

        // Check if user is logged in and is an admin
        if (authBean != null && authBean.estConnecte() && "ADMIN".equals(authBean.getInternauteConnecte().getRole())) {
            // User is admin, allow access
            chain.doFilter(request, response);
        } else {
            // User is not admin or not logged in, redirect to login page
            res.sendRedirect(req.getContextPath() + "/login.xhtml");
        }
    }
}
