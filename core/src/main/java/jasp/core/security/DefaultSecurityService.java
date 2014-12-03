package jasp.core.security;

import com.google.common.collect.Maps;
import jasp.core.model.Group;
import jasp.core.model.Query;
import jasp.core.model.Role;
import jasp.core.model.User;
import jasp.core.util.DataSourceSupport;
import jasp.core.util.SQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

public class DefaultSecurityService extends DataSourceSupport implements SecurityService {

    static Logger LOG = LoggerFactory.getLogger(DefaultSecurityService.class);

    static final String TABLE_USER = "users";

    /**
     * db schema
     */
    String schema = "public";

    public DefaultSecurityService(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void init() throws Exception {
        Map<String,String> vars = Maps.newHashMap();
        vars.put("schema", schema);

        runScript("drop.sql", getClass(), vars);
        runScript("create.sql", getClass(), vars);
    }

//    @Override
//    public Iterable<User> users(Query q) {
//        return stream((cx,s) -> {
//            SQL sql = new SQL("SELECT * FROM %s.%s", schema, TABLE_USER).log(LOG);
//            return s.open(sql.compile(cx)).executeQuery();
//        }, (rs) ->
//            (User) new User()
//                .handle(rs.getString("handle"))
//                .fullName(rs.getString("full_name"))
//                .created(rs.getDate("created"))
//                .modified(rs.getDate("modified"))
//                .id(rs.getObject("id"))
//        );
//    }
//
//    @Override
//    public void insert(User user) {
//        run((cx,s) -> {
//           SQL sql =
//               new SQL("INSERT INTO %s.%s VALUES (handle, full_name, password, created, modified)", schema, TABLE_USER)
//               .a(" VALUES (?,?,?,now(),now())")
//               .p(user.handle())
//               .p(user.fullName())
//               .p(user.password()).log(LOG);
//
//            return s.open(sql.compile(cx)).executeUpdate();
//        });
//    }
//
//    @Override
//    public void update(User user) {
//        run((cx,s) -> {
//           SQL sql = new SQL("UPDATE %s SET (handle = ?, full_name = ?, password = ?, modified = now())", TABLE_USER)
//               .p(user.handle())
//               .p(user.fullName())
//               .p(user.password()).log(LOG);
//           return s.open(sql.compile(cx)).executeUpdate();
//        });
//    }
//
//    @Override
//    public void delete(User user) {
//        run((cx,s) -> {
//            SQL sql = new SQL("DELETE FROM %s ", TABLE_USER)
//                    .p(user.handle())
//                    .p(user.fullName())
//                    .p(user.password()).log(LOG);
//            return s.open(sql.compile(cx)).executeUpdate();
//        });
//    }
//
//    @Override
//    public List<Role> roles(User user) {
//        return null;
//    }
//
//    @Override
//    public Iterable<Group> groups(Query q) {
//        return null;
//    }
//
//    @Override
//    public void insert(Group group) {
//
//    }
//
//    @Override
//    public void update(Group group) {
//
//    }
//
//    @Override
//    public void delete(Group group) {
//
//    }
//
//    @Override
//    public List<Group> groups(User user) {
//        return null;
//    }
//
//    @Override
//    public Iterable<Role> roles(Query q) {
//        return null;
//    }
//
//    @Override
//    public void insert(Role role) {
//
//    }
//
//    @Override
//    public void update(Role role) {
//
//    }
//
//    @Override
//    public void delete(Role role) {
//
//    }

}
