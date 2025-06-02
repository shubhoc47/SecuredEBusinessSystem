package Authentication.Filters;

import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import Authentication.Beans.AutenticationBean; // Ensure this is the correct package
import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebFilter; // Recommended for modern Jakarta EE

// @WebFilter("/*") // Alternative to web.xml configuration for Jakarta EE 6+
public class LoginFilter implements Filter {

    @Inject
    AutenticationBean session; // CDI will inject the session-scoped bean

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization code, if any
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
       HttpServletRequest req = (HttpServletRequest) request;
       HttpServletResponse resp = (HttpServletResponse) response;
       String url = req.getRequestURI();
       String contextPath = req.getServletContext().getContextPath();

       // Pages accessible only AFTER login
       String[] afterLog = {
           contextPath + "/default.xhtml",
           contextPath + "/newLaptop.xhtml",
           contextPath + "/newSmartphone.xhtml",
           contextPath + "/listLaptops.xhtml",
           contextPath + "/listSmartphones.xhtml",
           contextPath + "/listAllProducts.xhtml",
//           contextPath + "/searchProduct.xhtml",
//           contextPath + "/searchProductResults.xhtml",
           contextPath + "/searchLaptop.xhtml",  
           contextPath + "/searchPhone.xhtml", 
           contextPath + "/newCustomer.xhtml",
           contextPath + "/listCustomers.xhtml",
           contextPath + "/customerDetails.xhtml",
           contextPath + "/searchCustomer.xhtml",
//           contextPath + "/searchCustomerResults.xhtml",
           contextPath + "/customerMultiResults.xhtml", 
           // Add Order pages here when created:
           // contextPath + "/newOrder.xhtml",
           // contextPath + "/listOrders.xhtml",
           // contextPath + "/searchOrder.xhtml",
           // contextPath + "/searchOrderResults.xhtml",
           contextPath + "/logout.xhtml" // logout.xhtml itself is a protected trigger
       };

       // Pages accessible only BEFORE login (or public)
       String[] beforeLogOrPublic = {
           contextPath + "/login.xhtml",
           contextPath + "/register.xhtml",
           contextPath + "/emailVerification.xhtml",
           contextPath + "/emailRecovery.xhtml",
           contextPath + "/userRecovery.xhtml",
           contextPath + "/index.xhtml" // Assuming index is public and shows login/register
       };


       if (session == null || !session.isLogged()) {
           // User is NOT logged in
           boolean needsLogin = false;
           for (String protectedUrl : afterLog) {
                // Check if the current URL (ignoring query strings) ends with a protected page name
                if (url.endsWith(protectedUrl.substring(contextPath.length())) && !protectedUrl.endsWith("logout.xhtml")) {
                    // More robust check if using full paths from afterLog
                    // if (url.equals(protectedUrl)) { // This would require afterLog to have full exact URLs
                    needsLogin = true;
                    break;
                }
           }

           if (needsLogin) {
               resp.sendRedirect(contextPath + "/login.xhtml");
           } else {
               // Allow access to public pages or login page itself
               chain.doFilter(request, response);
           }
       } else {
           // User IS logged in
           if (url.endsWith("logout.xhtml")) {
               if (session != null) {
                   session.logout(); 
               }
               // Invalidate session is now handled inside session.logout()
               resp.sendRedirect(contextPath + "/login.xhtml");
           } else {
               boolean isOnBeforeLoginPage = false;
               for (String beforeLoginUrl : beforeLogOrPublic) {
                   // if (url.equals(beforeLoginUrl)) {
                   if (url.endsWith(beforeLoginUrl.substring(contextPath.length()))) {
                        if(!beforeLoginUrl.endsWith("index.xhtml")){ // Allow logged in user to go to index
                            isOnBeforeLoginPage = true;
                            break;
                        }
                   }
               }
               if (isOnBeforeLoginPage) {
                   // If logged in user tries to access login/register, redirect to dashboard
                   resp.sendRedirect(contextPath + "/default.xhtml");
               } else {
                   // Allow access to protected pages
                   chain.doFilter(request, response);
               }
           }
       }
    }

    @Override
    public void destroy() {
    }
}