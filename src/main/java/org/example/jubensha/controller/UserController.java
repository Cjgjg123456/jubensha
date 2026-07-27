package org.example.jubensha.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.example.jubensha.common.Result;
import org.example.jubensha.dto.LoginRequest;
import org.example.jubensha.dto.RegisterRequest;
import org.example.jubensha.entity.User;
import org.example.jubensha.entity.UserRegistrationHistory;
import org.example.jubensha.entity.UserStatistics;
import org.example.jubensha.service.UserService;
import org.example.jubensha.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @Autowired
    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @PostMapping("/login")
    public Result<User> login(@RequestBody @Valid LoginRequest loginRequest) {
        try {
            User user = userService.login(loginRequest);
            if (user == null) {
                return Result.fail("用户名或密码错误");
            }
            return Result.success(user);
        } catch (Exception e) {
            return Result.fail("登录失败：" + e.getMessage());
        }
    }

    @PostMapping("/register")
    public Result<User> register(@RequestBody @Valid RegisterRequest registerRequest, HttpSession session) {
        try {
            User user = userService.register(registerRequest, session);
            return Result.success(user);
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            return Result.fail("注册失败：" + e.getMessage());
        }
    }

    @GetMapping("/profile")
    public Result<User> getProfile(@RequestParam String username) {
        try {
            if (username == null || username.trim().isEmpty()) {
                return Result.fail("用户名不能为空");
            }
            User user = userService.getUserByUsername(username);
            if (user == null) {
                return Result.fail("用户不存在");
            }
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("获取用户信息失败：" + e.getMessage());
        }
    }

    @PostMapping("/profile")
    public Result<Boolean> updateProfile(
            @RequestParam("username") String username,
            @RequestParam("nickname") String nickname,
            @RequestParam("realName") String realName,
            @RequestParam(value = "gender", required = false) Integer gender,
            @RequestParam(value = "hobbyType", required = false) String hobbyType,
            @RequestParam(value = "birthday", required = false) String birthday,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "profile", required = false) String profile,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar) {
        try {
            // 先查到用户ID（UPDATE需要userId作为WHERE条件）
            User existing = userService.getUserByUsername(username);
            if (existing == null) {
                return Result.fail("用户不存在");
            }

            User user = new User();
            user.setUserId(existing.getUserId());
            user.setUsername(username);
            user.setNickname(nickname);
            user.setRealName(realName);
            user.setGender(gender);
            user.setHobbyType(hobbyType);
            if (birthday != null && !birthday.isEmpty()) {
                user.setBirthday(LocalDate.parse(birthday));
            }
            user.setCity(city);
            user.setProfile(profile);

            boolean success = userService.updateUserProfile(user, avatar);
            return success ? Result.success(true) : Result.fail("更新资料失败");
        } catch (Exception e) {
            return Result.fail("更新用户信息失败：" + e.getMessage());
        }
    }

    @GetMapping("/statistics")
    public Result<UserStatistics> getUserStatistics(@RequestParam String username) {
        try {
            User user = userService.getUserByUsername(username);
            if (user == null) return Result.fail("用户不存在");
            Long userId = user.getUserId();

            UserStatistics stat = new UserStatistics();
            stat.setPlayRecordCount(userMapper.countPlayRecords(userId));
            stat.setFollowCount(userMapper.countFollows(userId));
            stat.setHistoryCount(userMapper.countBrowseHistory(userId));
            stat.setCommentCount(userMapper.countComments(userId));
            stat.setCreationCount(userMapper.countCreations(userId));

            return Result.success(stat);
        } catch (Exception e) {
            return Result.fail("获取统计数据失败");
        }
    }

    // ================= 新增：统一的动态记录获取接口 =================
    @GetMapping("/records/{type}")
    public Result<List<Map<String, Object>>> getUserRecords(@RequestParam String username, @PathVariable String type) {
        try {
            User user = userService.getUserByUsername(username);
            if (user == null) return Result.fail("用户不存在");
            Long userId = user.getUserId();

            List<Map<String, Object>> list;
            switch (type) {
                case "play": list = userMapper.getPlayRecords(userId); break;
                case "follow": list = userMapper.getFollows(userId); break;
                case "browse": list = userMapper.getBrowseHistory(userId); break;
                case "comment": list = userMapper.getComments(userId); break;
                case "creation": list = userMapper.getCreations(userId); break;
                default: list = new ArrayList<>();
            }
            return Result.success(list);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("获取记录失败");
        }
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success(null);
    }

    @PostMapping("/follow")
    public Result<String> toggleFollow(@RequestBody Map<String, Object> payload) {
        try {
            String username = (String) payload.get("username");
            Long followedUserId = Long.valueOf(payload.get("followedUserId").toString());
            User currentUser = userService.getUserByUsername(username);
            if (currentUser == null) return Result.fail("用户不存在");

            Long userId = currentUser.getUserId();
            if (userId.equals(followedUserId)) return Result.fail("不能关注自己");

            int existing = userMapper.checkFollow(userId, followedUserId);
            if (existing > 0) {
                userMapper.deleteFollow(userId, followedUserId);
                return Result.success("取消关注");
            } else {
                int anyRecord = userMapper.checkFollowAny(userId, followedUserId);
                if (anyRecord > 0) {
                    userMapper.reFollow(userId, followedUserId);
                } else {
                    userMapper.insertFollow(userId, followedUserId);
                }
                return Result.success("关注成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("操作失败：" + e.getMessage());
        }
    }

    @GetMapping("/follows/ids")
    public Result<List<Long>> getFollowedIds(@RequestParam String username) {
        try {
            User user = userService.getUserByUsername(username);
            if (user == null) return Result.fail("用户不存在");
            List<Long> ids = userMapper.getFollowedUserIds(user.getUserId());
            return Result.success(ids);
        } catch (Exception e) {
            return Result.fail("查询失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/records/comment/{evaluateId}")
    public Result<String> deleteComment(@PathVariable Long evaluateId) {
        try {
            int rows = userMapper.deleteComment(evaluateId);
            return rows > 0 ? Result.success("删除成功") : Result.fail("删除失败，评价不存在");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("删除异常：" + e.getMessage());
        }
    }

    @GetMapping("/registration-history")
    public Result<Object> getUserRegistrationHistory(@RequestParam String username) {
        try {
            User user = userService.getUserByUsername(username);
            if (user == null) return Result.fail("用户不存在");
            UserRegistrationHistory history = userMapper.getUserRegistrationHistory(user.getUserId());
            return Result.success(history);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/my-publications")
    public Result<List<Map<String, Object>>> getMyPublications(@RequestParam String username) {
        try {
            User user = userService.getUserByUsername(username);
            if (user == null) return Result.fail("用户不存在");
            List<Map<String, Object>> publications = userMapper.getCreations(user.getUserId());
            return Result.success(publications);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/publication-stats")
    public Result<Map<String, Object>> getPublicationStats(@RequestParam String username) {
        try {
            User user = userService.getUserByUsername(username);
            if (user == null) return Result.fail("用户不存在");

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalScripts", userMapper.countCreations(user.getUserId()));
            stats.put("totalPlays", userMapper.countPlayRecords(user.getUserId()));
            stats.put("totalFollowers", userMapper.countFollows(user.getUserId()));
            stats.put("totalComments", userMapper.countComments(user.getUserId()));

            return Result.success(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("查询失败：" + e.getMessage());
        }
    }

}