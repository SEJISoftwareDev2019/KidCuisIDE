package com.seji.kidcuiside;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.sql.*;

@Controller
public class LoginController {

    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost/cuiside";

    @GetMapping("/login")
    public String login(Model model, @CookieValue(value = "id", defaultValue = "") String id) {
        /*FileData fd = new FileData(0, "Main.java",
                "public class Main {" +
                "\n    public static void main(String[] args) {" +
                "\n        System.out.println(\"Hello World!\");" +
                "\n    }" +
                "\n }");
        fd.write("TESTUSER");
        ProccessManager pm = new ProccessManager("");
        pm.compile(fd, System.in, System.out, System.err);
        pm.run(fd, null, null, null);
        **********Test Stuff*************
        */
        if(id.equals("")) {
            model.addAttribute("login", new LoginForm());
            return "login";
        }
        else {  //TODO check if id is valid
            return "redirect:/code";
        }
    }

    @PostMapping("/login")
    public String loginRequest(@ModelAttribute LoginForm login, HttpServletResponse response) {
        boolean correct = false;
        String user = login.getUsername();
        String password = login.getPassword();
        if(!isValidInput(user, password)) {
            return "redirect:/login";
        }
        else {
            String hash = Cryptor.hash("Kid" + user + "Cuis" + password + "IDE");
            response.addCookie(new Cookie("id", hash));
            Connection conn = null;
            Statement state = null;
            try {
                Class.forName(JDBC_DRIVER);
                conn = DriverManager.getConnection(DB_URL, "root", "blu");
                state = conn.createStatement();
                //selecting username-password pairs. yeah, this would be tedious for many users. shouldnt be a problem for us though
                String sql = "select username,password from users";
                ResultSet set = state.executeQuery(sql);
                while (set.next()) {
                    if (login.getUsername().equals(set.getString("username"))) {
                        if (login.getPassword().equals(set.getString("password"))) {
                            //this is a crude stand-in for future code that allows a user to sign in for themselves
                            //at the moment this is just confirmation that a username/password combination exists in the system
                            correct = true;
                        }
                    }
                }
                //everything after this is exception checking and closing things. BEST PRACTICES ONLY
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (state != null)
                        state.close();
                } catch (SQLException se2) {
                }
                try {
                    if (conn != null)
                        conn.close();
                } catch (SQLException se) {
                }
            }
        }
        return correct ? "redirect:/code" : "redirect:/login";
    }

    @GetMapping("/signup")
    public String signUp(Model model) {
        model.addAttribute("signup", new SignUpForm());
        return "signup";
    }

    @PostMapping("/signup")
    public String signUp(@ModelAttribute SignUpForm signup) {
        System.out.println(signup);
        if(signup.getPassword().equals(signup.getConfirmPassword()) && isValidInput(signup.getUsername(), signup.getPassword())) {
            String hash = Cryptor.hash("Kid" + signup.getUsername() + "Cuis" + signup.getPassword() + "IDE");
            Connection conn = null;
            Statement state = null;
            try {
                //maven basically did all the classpath stuff for me
                Class.forName(JDBC_DRIVER);
                conn = DriverManager.getConnection(DB_URL, "root", "blu");
                state = conn.createStatement();
                //retrieving infromation and putting it in the correct columns
                String sql = "insert into users (username,firstname,lastname,email,password,filecount) " +
                        "values (\'" + signup.getUsername() + "\',\'" + signup.getFirstname() + "\',\'" + signup.getLastname() + "\',\'" + signup.getEmail() + "\',\'" + signup.getPassword() + "\',0);";
                state.executeUpdate(sql);
                //everything after this is exception checking and closing things. BEST PRACTICES ONLY
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (state != null)
                        state.close();
                } catch (SQLException se2) {
                }
                try {
                    if (conn != null)
                        conn.close();
                } catch (SQLException se) {
                }
            }
            return "redirect:/signupconfirm";
        }
        //Try again skrub
        return "redirect:/signup";
    }

    @GetMapping("/signupconfirm")
    public String signUpConfirm() {
        return "signupConfirm";
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        response.addCookie(new Cookie("id", ""));
        return "redirect:/";
    }

    private boolean isValidInput(String user, String password) {
        return !(user == null || user.length() < 5 || user.length() > 64 || user.contains("\\W") || (user+password).contains("\\s") || password == null || password.length() < 6 || password.length() > 128);
    }
}