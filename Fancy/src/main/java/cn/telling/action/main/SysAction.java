package cn.telling.action.main;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.danga.MemCached.MemCachedClient;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cn.telling.action.BaseController;
import cn.telling.action.main.service.PagingService;
import cn.telling.action.user.util.UserUtils;
import cn.telling.common.StringUtils;
import cn.telling.common.Pager.PageVo;
import cn.telling.common.Pager.Pager;
import cn.telling.common.constants.Constants;
import cn.telling.common.servlet.ValidateCodeServlet;
import cn.telling.common.uitl.CacheUtils;
import cn.telling.common.uitl.IdGen;
import cn.telling.common.uitl.StringUtil;
import cn.telling.common.uitl.TimestampTypeAdapter;
import cn.telling.log.service.IUserLoginLogService;
import cn.telling.log.vo.UserLoginLog;
import cn.telling.menu.service.IMenuService;
import cn.telling.menu.vo.Menu;
import cn.telling.menu.vo.TreeNode;
import cn.telling.shirocontroller.FormAuthenticationFilter;
import cn.telling.shirocontroller.ShiroRealm.Principal;
import cn.telling.user.service.IUserService;
import cn.telling.user.vo.User;
import cn.telling.utils.LogType;
import cn.telling.utils.Paging;
import cn.telling.utils.StringHelperTools;
import cn.telling.utils.TCPIPUtil;
import cn.telling.web.AuthImg;
import cn.telling.web.MethodLog;

/**
 * @Title: SysAction.java
 * @Package cn.telling.action.main
 * @Description: 系统相关
 * @author 操圣
 * @date 2015-12-7 上午11:00:31
 * @version V1.0
 */
@Controller
public class SysAction extends BaseController{
    
 

	private AuthImg ai = null;
	@Resource
	public PagingService pagingService;

	@Resource
	private IUserService userService;

	@Resource
	private org.apache.shiro.cache.CacheManager shiroCacheManager;

	@Resource
	IUserLoginLogService userLoginLogService;

	@Resource
	IMenuService menuService;
  	
  @Autowired
  private MemCachedClient memCachedClient;
  
	@RequestMapping("/viewLeftMenuJson")
	@MethodLog(remark = "系统左侧菜单查询")
	public void getLeftMenuJson(HttpServletResponse response, String index) {
		response.setContentType("text/html;charset=utf-8");
		// shiro几乎所有的环境下，都可以通过这种方式获取当前用户
		User user =UserUtils.getUser();
		BigDecimal userid = user.getId();
		List<Menu> listpm = new ArrayList<Menu>();
		try {
			listpm = menuService.findMenuByUserId(userid);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		// 拉出数据库的数据，放入list2中
		List<TreeNode> list = new ArrayList<TreeNode>();
		Map<Object, Object> map = new HashMap<Object, Object>();
		// 拉出数据库的数据，放入list2中
		// Map pmap = new HashMap<String, TreeNode>();
		// ArrayList<PusMenu> list2 = (ArrayList<TDict>)this.find(hql);

		// 将listpm中的数据，转换成TreeNode类型，放入Map中备用
		for (Menu pm : listpm) {
			TreeNode node = new TreeNode();
			node.setId(pm.getId());
			node.setText(pm.getName());

			Map<String, String> attributes = new HashMap<String, String>();
			attributes.put("url", pm.getPageUrl());

			node.setAttribute(attributes);
			// jdbc方式数据集的特殊处理
			if (pm.getPm() != null) {
				node.setParentId(pm.getPm().getId());
			} else {
				pm.setPm(menuService.getById(pm.getMenupId()));
				if (pm.getPm() != null) {
					pm.setPm(menuService.getById(pm.getMenupId()));
				}
			}
			// node.setParentId(pm.getId());
			map.put(pm.getId(), node);
		}

		// 遍历listpm的数据，把每个节点加入他的父节点的孩子List
		for (Menu pm : listpm) {

			if (pm.getPm() != null) {

				if (pm.getPm().getId() == null) {
					list.add((TreeNode) map.get(pm.getId()));
				} else {

					BigDecimal pidInteger = pm.getPm().getId();
					TreeNode pnode = (TreeNode) map.get(pidInteger);
					TreeNode cnode = (TreeNode) map.get(pm.getId());
					// if (pnode != null) {
					// pnode.addChild(cnode);
					// }
					if (pnode == null) { // 当分配用户子菜单的时候没有分配 父菜单的情况 如果 不做这个判断
											// 将会无法显示菜单
						pnode = new TreeNode();
						pnode.setId(new BigDecimal(0));
						pnode.addChild(cnode);
						list.add((TreeNode) map.get(pm.getId()));
					} else {
						pnode.addChild(cnode);
					}

				}
			} else {
				list.add((TreeNode) map.get(pm.getId()));
			}
		}

		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.setDateFormat("yyyy-MM-dd hh:mm:ss");
		gsonBuilder.registerTypeAdapter(Timestamp.class, new TimestampTypeAdapter());
		Gson gson = gsonBuilder.create();

		String indexis = StringUtil.stringIsNull(index);

		// 选择该菜单后子菜单置为顶级菜单
		TreeNode dj = new TreeNode();
		dj.setId(new BigDecimal(0));
		dj.setText("顶级菜单");
		if (!indexis.equals("true")) {
			list.add(dj);
		}

		String json = gson.toJson(list);

		PrintWriter out = null;
		try {
			out = response.getWriter();
		} catch (IOException e) {
			e.printStackTrace();
		}
		out.write(json);
		out.flush();
	}

	@RequestMapping("validate")
	public void validate(HttpServletRequest request, HttpServletResponse response) {
		ai = new AuthImg();
		ai.service(request, response);
	}

	@RequestMapping(value="login",method=RequestMethod.GET)
	public String login() {
	  return "login";
	}
	   
	 
	/****
	 * 用户登录
	 * 
	 * @param
	 * @author 操圣
	 * @date 2015-4-27 下午3:35:32
	 * @version V1.0
	 */
	@MethodLog(remark = "用户登录", operType = LogType.LOGIN)
	@RequestMapping(value = "login", method = RequestMethod.POST)
	public String userLogin(User user, ModelMap model, HttpServletRequest request, HttpServletResponse response) {

		// if (userName != null && !"".equals(userName) && passWord != null &&
		// !"".equals(passWord)) {
		// String uId = String.valueOf(userService.getUserId(userName));
		// if (uId != null) {
		// String pwd = MD5Tool.encryptionPwd(passWord, Long.parseLong(uId));
		// Users users = userService.userLogin(userName, pwd);
		// if (users != null) {
		// String validate = (String)
		// request.getSession().getAttribute("validate");
		// String vali = request.getParameter("validate");
		// if (validate != null && vali != null &&
		// vali.equalsIgnoreCase(validate)) {
		// request.getSession().setAttribute("userId", users.getId());
		// request.getSession().setAttribute("userName", users.getUsername());
		// request.getSession().setAttribute("lastTime", users.getLastTime());
		// request.getSession().setAttribute("email", users.getEmail());
		// return "redirect:index.html";
		// }
		// }
		// }
		// model.put("msg", "登录失败");
		// }
//		 PasswordService svc = new DefaultPasswordService();
//		 String encrypted = svc.encryptPassword(users.getPassword());
	    Principal principal = UserUtils.getPrincipal();
        
        // 如果已经登录，则跳转到管理首页
        if(principal != null){
            return "redirect:/";
        }

		String validate = (String) request.getSession().getAttribute("validate");
		String vali = request.getParameter("validate");
		if (StringHelperTools.nvl(user.getAccount()).equals("")) {
			model.addAttribute("message", "帐号不能为空!");
			return "forward:login.html";
		}
		else if (StringHelperTools.nvl(user.getPassword()).equals("")) {
			model.addAttribute("message", "密码不能为空!");
			return "forward:login.html";
		}
		else if (StringHelperTools.nvl(vali).equals("")) {
			model.addAttribute("message", "验证码不能为空!");
			return "forward:login.html";
		}
		// 比对
		else if (StringHelperTools.nvl(validate).equals("")) {
			model.addAttribute("message", "验证码过期,请重新登录!");
			return "forward:login.html";
		}
		else if (!validate.equalsIgnoreCase(vali)) {
			// throw new IncorrectCaptchaException("验证码错误！");
			model.addAttribute("message", "验证码错误!");
			return "forward:login.html";
		}
		
		
		
		/*UsernamePasswordToken token = new UsernamePasswordToken(user.getAccount(), user.getPassword());
		token.setRememberMe(remembered);
		try {
		  subj.login(token);
		} catch (UnknownAccountException uae) {
			model.addAttribute("message", "账号不存在!");
			return "forward:login.html";
		} catch (IncorrectCredentialsException ice) {
			model.addAttribute("message", "密码错误!");
			return "forward:login.html";
		} catch (LockedAccountException lae) {
			model.addAttribute("message", "账号被锁定!");
			return "forward:login.html";
		} catch (AuthenticationException e) {
			model.addAttribute("message", "认证错误!");
			return "forward:login.html";
		} catch (Exception e) {
			model.addAttribute("message", "未知错误,请联系管理员.");
			 e.printStackTrace();
			logger.error("未知错误,请联系管理员", e);
			return "forward:login.html";
		}*/
		
		String username = WebUtils.getCleanParam(request, FormAuthenticationFilter.DEFAULT_USERNAME_PARAM);
        boolean rememberMe = WebUtils.isTrue(request, FormAuthenticationFilter.DEFAULT_REMEMBER_ME_PARAM);
        boolean mobile = WebUtils.isTrue(request, FormAuthenticationFilter.DEFAULT_MOBILE_PARAM);
        String exception = (String)request.getAttribute(FormAuthenticationFilter.DEFAULT_ERROR_KEY_ATTRIBUTE_NAME);
        String message = (String)request.getAttribute(FormAuthenticationFilter.DEFAULT_MESSAGE_PARAM);
        
        if (StringUtils.isBlank(message) || StringUtils.equals(message, "null")){
            message = "用户或密码错误, 请重试.";
        }

        model.addAttribute(FormAuthenticationFilter.DEFAULT_USERNAME_PARAM, username);
        model.addAttribute(FormAuthenticationFilter.DEFAULT_REMEMBER_ME_PARAM, rememberMe);
        model.addAttribute(FormAuthenticationFilter.DEFAULT_MOBILE_PARAM, mobile);
        model.addAttribute(FormAuthenticationFilter.DEFAULT_ERROR_KEY_ATTRIBUTE_NAME, exception);
        model.addAttribute(FormAuthenticationFilter.DEFAULT_MESSAGE_PARAM, message);
        
        if (logger.isDebugEnabled()){
            logger.debug("login fail, active session size:, message: {}, exception: {}",  message, exception);
        }
        
        // 非授权异常，登录失败，验证码加1。
        if (!UnauthorizedException.class.getName().equals(exception)){
            model.addAttribute("isValidateCodeLogin", isValidateCodeLogin(username, true, false));
        }
        
        // 验证失败清空验证码
        request.getSession().setAttribute(ValidateCodeServlet.VALIDATE_CODE, IdGen.uuid());
        
        // 如果是手机登录，则返回JSON字符串
        if (mobile){
            return renderString(response, model);
        }
        
		
		
		
		
		logger.info("login sucesss!");
		UserLoginLog ulog = new UserLoginLog();
		ulog.setType(0);
		ulog.setUseraccount(user.getAccount());
		ulog.setIp(TCPIPUtil.getIpAddr(request));
		userLoginLogService.saveLog(ulog);
		
		// 登录成功后，验证码计算器清零
        //isValidateCodeLogin(principal.getLoginName(), false, true);
		System.out.println(UserUtils.getUser().getUsername());
		return "index";
	}

	   /**
     * 登录成功，进入管理首页
     */
    @RequiresPermissions("user")
    @RequestMapping(value = "")
    public String index(HttpServletRequest request, HttpServletResponse response) {
        return "index";
    }
	/**
     * 是否是验证码登录
     * 
     * @param useruame
     *            用户名
     * @param isFail
     *            计数加1
     * @param clean
     *            计数清零
     * @return
     */
    @SuppressWarnings("unchecked")
    public static boolean isValidateCodeLogin(String useruame, boolean isFail,
            boolean clean) {
        Map<String, Integer> loginFailMap = (Map<String, Integer>) CacheUtils
                .get("loginFailMap");
        if (loginFailMap == null) {
            loginFailMap = Maps.newHashMap();
            CacheUtils.put("loginFailMap", loginFailMap);
        }
        Integer loginFailNum = loginFailMap.get(useruame);
        if (loginFailNum == null) {
            loginFailNum = 0;
        }
        if (isFail) {
            loginFailNum++;
            loginFailMap.put(useruame, loginFailNum);
        }
        if (clean) {
            loginFailMap.remove(useruame);
        }
        return loginFailNum >= 3;
    }
	@RequestMapping(value = "/noAuth", method = RequestMethod.GET)
	public ModelAndView noAuthAction() {
		ModelAndView mav = new ModelAndView("/NoCompetence");

		return mav;
	}

	@RequestMapping("/tForm")
	public String tForm(HttpServletRequest request) {

		return "/user/tForm";
	}

	
    @RequestMapping("/i")
    public String testLogin(ModelMap map, HttpServletRequest req, Paging pa) {
        /*******老板分页(不共通)开始****/
        /**** 首先当前也不能小于1,第一页从1开始 ***/
//        if (pa.getNowPage() < 1) {
//            pa.setNowPage(1);
//        }

        /** 总条数 **/

//        pa.setTotal(pagingService.getPageCount());

        /** 总页数 如果不能整除就加一页 **/

//        pa.setCountPage(pa.getTotal() % pa.getPageSize() == 0 ? pa.getTotal() / pa.getPageSize()
//                        : pa.getTotal() / pa.getPageSize() + 1);
//        if (pa.getNowPage() > pa.getCountPage()) {
//            pa.setNowPage(pa.getCountPage());
//        }
//        if (pa.getNowPage() == 0) {
//            pa.setStartIndex(pa.getNowPage() * pa.getPageSize());
//            pa.setEndIndex(pa.getPageSize());
//        } else {
//            pa.setStartIndex((pa.getNowPage() - 1) * pa.getPageSize());
//            pa.setEndIndex(pa.getPageSize());
//        }
//        List<Users> data = pagingService.getPageData(pa);
//        map.put("data", data);
//        map.put("page", pa);
        /*******老板分页(不共通)结束****/
        String pageNow = StringHelperTools.nvl(req.getParameter("page"));
        PageVo pageVo = Pager.getPager();
        if ("".equals(pageNow)) {
            pageVo.setPageNow(1);
        } else {
            pageVo.setPageNow(Integer.parseInt(pageNow));
        }
        pageVo.setMessageForPage(10);
        pageVo.setUrl(req.getContextPath() + "/i.html");
        List<User> userLs = pagingService.getPageData(pageVo);
        map = Pager.setPageerToPage(map, pageVo);
        map.put("userLs", userLs);
        map.put("page", pageNow);
        return "/user/paging";
    }


	 
	 
  @RequestMapping("/loginout")
  public String loginout(HttpServletRequest request, HttpServletResponse reponse)
  {
    Subject subject = SecurityUtils.getSubject();
    User user =UserUtils.getUser();
    if (user != null) {
      Cookie cookie = new Cookie(user.getUsername(), null);
      cookie.setMaxAge(0); // 如果参数是0，就说明立即删除 reponse.addCookie(cookie); }

      if (subject != null) {
        subject.logout();
      } //
      request.getSession().invalidate(); // 刷新权限缓存
      shiroCacheManager.getCache(Constants.authorizationCacheName).clear();

      // org.apache.shiro.web.filter.authc.LogoutFilter // 记录日志 //
      UserLoginLog ulog = new UserLoginLog(); // ulog.setType(1); //
      ulog.setUseraccount(user.getAccount()); //
      ulog.setIp(TCPIPUtil.getIpAddr(request)); //
      userLoginLogService.saveLog(ulog);
    }
    return "redirect:/user/login.html";
  }
	 
	@RequestMapping(value = "/userlogout")
	@MethodLog(remark = "用户退出")
	public String userlogout(HttpServletRequest request, HttpServletResponse httpresponse) {
		Subject subject = SecurityUtils.getSubject();

		User user =UserUtils.getUser();
		if (user != null) {
			Cookie cookie = new Cookie(user.getUsername(), null);
			cookie.setMaxAge(0); // 如果参数是0，就说明立即删除
			httpresponse.addCookie(cookie);
		}

		if (subject != null) {
			subject.logout();
		}
		// request.getSession().invalidate();
		// 刷新权限缓存
		shiroCacheManager.getCache(Constants.authorizationCacheName).clear();

		// org.apache.shiro.web.filter.authc.LogoutFilter
		// 记录日志
		UserLoginLog ulog = new UserLoginLog();
		ulog.setType(new Integer(1));
		ulog.setUseraccount(user.getAccount());
		ulog.setIp(TCPIPUtil.getIpAddr(request));
		userLoginLogService.saveLog(ulog);
		return "redirect:login.html";
	};

	@RequestMapping("/myServletLogin")
	public String myServletLogin(HttpServletRequest request) {
		request.getSession().removeAttribute("userId");
		return "/user/mylogin";
	}

	@RequestMapping("/ftl")
	public String test(HttpServletRequest request, ModelMap map) {
		return "/ftl/mytest";
	}

	/***
	 * 
	 *404 
	 */
	@RequestMapping("/404")
	public String FileNotFound()
	{
	    return "/error/404";
	}

	/***
     * 
     *404 
	 * @throws Exception 
     */
    @RequestMapping("/ceshi")
    public String ceshi() throws Exception
    {
       throw new Exception("出错");
    }
	public static final String DEFAULT_CAPTCHA_PARAM = "captcha";

	private String captchaParam = DEFAULT_CAPTCHA_PARAM;

	public String getCaptchaParam() {
		return captchaParam;
	}

	public void setCaptchaParam(String captchaParam) {
		this.captchaParam = captchaParam;
	}

	protected String getCaptcha(ServletRequest request) {
		return WebUtils.getCleanParam(request, getCaptchaParam());
	}



	@RequestMapping("/menus")
	public String getMenus()
	{
        return "/sys/listMenu";
	    
	}
	
	@RequestMapping("/viewAllMenuJson")
    @MethodLog(remark = "菜单列表数据查看")
    public void getViewAllMenuJson(@RequestParam(required = false, defaultValue = "") String name, HttpServletResponse response) {
	  memCachedClient.add("testjava", "hhaha");
	  System.out.println("==========================读取memcached---"+memCachedClient.get("testjava"));
	  CacheUtils.put("testjava", "fasdfasdfasdf");
	  System.out.println("ehcache读取：=================："+CacheUtils.get("testjava"));
       response.setContentType("text/html;charset=utf-8");

        // shiro几乎所有的环境下，都可以通过这种方式获取当前用户
//        Users user = (Users) SecurityUtils.getSubject().getPrincipal();


        List<Menu> listpm = menuService.findMenus(name);
        // 拉出数据库的数据，放入list2中

        List<Menu> list = new ArrayList<Menu>();
        Map<BigDecimal, Menu> map = new HashMap<BigDecimal, Menu>();
        // 拉出数据库的数据，放入list2中
//        HashMap<String, Menu> pmap = new HashMap<String, Menu>();
        // ArrayList<PusMenu> list2 = (ArrayList<TDict>)this.find(hql);

        // 将listpm中的数据，转换成TreeNode类型，放入Map中备用
        for (Menu pm : listpm) {
            Menu node = new Menu();
            node.setId(pm.getId());
            node.setName(pm.getName());
            node.setCreateTime(pm.getCreateTime());
            node.setDescription(pm.getDescription());
            node.setMenupId(pm.getMenupId());
            node.setPageUrl(pm.getPageUrl());
            node.setState(pm.getState());
            node.setType(pm.getType());

            // Map<String, String> attributes = new HashMap<String, String>();
            // attributes.put("url", pm.getPageurl());

            // node.setAttribute(attributes);
            // if (pm.getPm() != null) {
            // node.setParentId(pm.getPm().getId());
            // }

            map.put(pm.getId(), node);
            if (pm.getPm()==null) {
                pm.setPm(menuService.getById(pm.getMenupId()));
            }
        }

        // 遍历listpm的数据，把每个节点加入他的父节点的孩子List

        for (Menu pm : listpm) {

            if (pm.getPm() != null) {

                if (pm.getPm().getId() == null) {
                    list.add((Menu) map.get(pm.getId()));
                } else {

                    BigDecimal pidInteger = pm.getPm().getId();
                    Menu pnode = (Menu) map.get(pidInteger);
                    Menu cnode = (Menu) map.get(pm.getId());

                    if (pnode == null) { // 当分配用户子菜单的时候没有分配 父菜单的情况 如果 不做这个判断
                                            // 将会无法显示菜单
                        pnode = new Menu();
                        pnode.setId(new BigDecimal(0));
                        pnode.addChild(cnode);
                        list.add((Menu) map.get(pm.getId()));
                    } else {
                        pnode.addChild(cnode);
                    }
                    // if (cnode != null) {
                    //
                    // list.add((PusMenuTreegridBean) map.get(pm.getId()));
                    // }
                    // if(pnode==null){
                    // pnode=new PusMenuTreegridBean();
                    // pnode.setId(0);
                    // }
                    // pnode.addChild(cnode);
                    // if (pnode != null) {
                    // pnode.addChild(cnode);
                    // }
                    // if (!StringUtil.stringIsNull(name).equals("")) {
                    // list.add((PusMenuTreegridBean) map.get(pm.getId()));
                    // }
                }

            } else {
                list.add((Menu) map.get(pm.getId()));

            }

        }

        // Map mapend = new HashMap<Integer, PusMenuTreegridBean>();
        //
        // for (int i = 0; i < list.size(); i++) {
        // int id = list.get(i).getId();
        // // PusMenuTreegridBean pb=
        // mapend.put(id, list.get(i));
        // }
        //
        // List<PusMenuTreegridBean> listend = new
        // ArrayList<PusMenuTreegridBean>();
        // Set set = mapend.keySet();
        //
        // for (Iterator iter = set.iterator(); iter.hasNext();) {
        // Integer key = (Integer) iter.next();
        // PusMenuTreegridBean value = (PusMenuTreegridBean) map.get(key);
        // listend.add(value);
        // // System.out.println(key+"===="+value);
        // }

        // List<PusMenuTreegridBean> listend=new
        // ArrayList<PusMenuTreegridBean>();
        // for (Object value : mapend.values()) {
        // listend.add(value.)
        // }

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setDateFormat("yyyy-MM-dd hh:mm:ss");
        gsonBuilder.registerTypeAdapter(Timestamp.class, new TimestampTypeAdapter());
        Gson gson = gsonBuilder.create();
        String json = gson.toJson(list);
        PrintWriter out = null;
        try {
            out = response.getWriter();
        } catch (IOException e) {
            e.printStackTrace();
        }
        out.write(json);
        out.flush();
    }
}
