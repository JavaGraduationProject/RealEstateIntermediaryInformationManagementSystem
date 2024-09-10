package com.learn.controller;

import com.learn.entity.SysUserEntity;
import com.learn.service.SysUserRoleService;
import com.learn.service.SysUserService;
import com.learn.utils.PageUtils;
import com.learn.utils.Query;
import com.learn.utils.R;
import com.learn.utils.ShiroUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统用户
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2018年10月31日 上午10:40:10
 */
@RestController
@RequestMapping("/sys/user")
public class SysUserController extends AbstractController {
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysUserRoleService sysUserRoleService;

    /**
     * 所有用户列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {

        //查询列表数据
        Query query = new Query(params);
        List<SysUserEntity> userList = sysUserService.queryList(query);
        int total = sysUserService.queryTotal(query);

        PageUtils pageUtil = new PageUtils(userList, total, query.getLimit(), query.getPage());

        return R.ok().put("page", pageUtil);
    }

    @RequestMapping("/list2")
    public R list2(@RequestParam Map<String, Object> params) {

        //查询列表数据
        Query query = new Query(params);
        List<SysUserEntity> userList = sysUserService.queryList(query);

        return R.ok().put("list", userList);
    }

    /**
     * 获取登录的用户信息
     */
    @RequestMapping("/info")
    public R info() {
        return R.ok().put("user", this.sysUserService.queryObject(getUser().getUserId()));
    }

    /**
     * 修改登录用户密码
     */
    @RequestMapping("/password")
    public R password(String password, String newPassword) {

        //sha256加密
        password = new Sha256Hash(password).toHex();
        //sha256加密
        newPassword = new Sha256Hash(newPassword).toHex();

        //更新密码
        int count = sysUserService.updatePassword(getUserId(), password, newPassword);
        if (count == 0) {
            return R.error("原密码不正确");
        }

        //退出
        ShiroUtils.logout();

        return R.ok();
    }

    @RequestMapping("/updateInfo")
    public R updateInfo(@RequestBody SysUserEntity user) {
        this.sysUserService.update(user);
        return R.ok();
    }

    /**
     * 用户信息
     */
    @RequestMapping("/info/{userId}")
    public R info(@PathVariable("userId") Long userId) {
        SysUserEntity user = sysUserService.queryObject(userId);

        //获取用户所属的角色列表
        List<Long> roleIdList = sysUserRoleService.queryRoleIdList(userId);
        user.setRoleIdList(roleIdList);

        return R.ok().put("user", user);
    }

    /**
     * 保存用户
     */
    @RequestMapping("/save")
    public R save(@RequestBody SysUserEntity user) {

        user.setCreateUserId(getUserId());
        sysUserService.save(user);

        return R.ok();
    }

    /**
     * 修改用户
     */
    @RequestMapping("/update")
    public R update(@RequestBody SysUserEntity user) {

        user.setCreateUserId(getUserId());
        sysUserService.update(user);

        return R.ok();
    }

    /**
     * 删除用户
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] userIds) {
        if (ArrayUtils.contains(userIds, 1L) || ArrayUtils.contains(userIds, -1L)) {
            return R.error("系统管理员不能删除");
        }

        if (ArrayUtils.contains(userIds, getUserId())) {
            return R.error("当前用户不能删除");
        }

        sysUserService.deleteBatch(userIds);

        return R.ok();
    }
}
