package cn.telling.web;

import java.awt.Menu;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import cn.telling.log.service.ISysLogService;
import cn.telling.log.vo.Syslog;
import cn.telling.role.vo.Role;
import cn.telling.shirocontroller.ShiroRealm.Principal;
import cn.telling.user.vo.User;
import cn.telling.utils.LogUtils;
import cn.telling.utils.StringHelperTools;
import cn.telling.utils.TCPIPUtil;


/**
 * 记录系统登录的日志
 * 
 * 
 * 
 * 
 * @Aspect 实现spring aop 切面（Aspect）： 一个关注点的模块化，这个关注点可能会横切多个对象。事务管理是J2EE应用中一个关于横切关注点的很好的例子。 在Spring
 *         AOP中，切面可以使用通用类（基于模式的风格） 或者在普通类中以 @Aspect 注解（@AspectJ风格）来实现。
 * 
 *         AOP代理（AOP Proxy）： AOP框架创建的对象，用来实现切面契约（aspect contract）（包括通知方法执行等功能）。 在Spring中，AOP代理可以是JDK动态代理或者CGLIB代理。
 *         注意：Spring 2.0最新引入的基于模式（schema-based ）风格和@AspectJ注解风格的切面声明，对于使用这些风格的用户来说，代理的创建是透明的。
 * @author caosheng
 * 
 * 
 *  com.sample.service.impl..*. *(..))

execution()是最常用的切点函数，其语法如下所示：

整个表达式可以分为五个部分：

1、execution(): 表达式主体。

2、第一个*号：表示返回类型， *号表示所有的类型。

3、包名：表示需要拦截的包名，后面的两个句点表示当前包和当前包的所有子包，com.sample.service.impl包、子孙包下所有类的方法。

4、第二个*号：表示类名，*号表示所有的类。

5、*(..):最后这个星号表示方法名，*号表示所有的方法，后面括弧里面表示方法的参数，两个句点表示任何参数


 */
@Component
@Aspect
public class LogService {

	// 记录日志
    protected final Logger logger = Logger.getLogger(this.getClass());
	@Autowired
	private ISysLogService logService;

	public LogService()
	{
	    LogUtils.debug("Aop init");
	}

	/**
	 * 在Spring 2.0中，Pointcut的定义包括两个部分：Pointcut表示式(expression)和Pointcut签名(signature )。让我们先看看execution表示式的格式：
	 * 括号中各个pattern分别表示修饰符匹配（modifier-pattern?）、返回值匹配（ret -type-pattern）、类路径匹配（declaring
	 * -type-pattern?）、方法名匹配（name-pattern）、参数匹配（(param -pattern)）、异常类型匹配（throws-pattern?），其中后面跟着“?”的是可选项。
	 * @param point
	 * @throws Throwable
	 */

	@Pointcut("@annotation(cn.telling.web.MethodLog)")
	public void methodCachePointcut()
	{
	    LogUtils.debug("Aop Pointcut invoke");
	}

//	 @Before("execution(* cn.telling.action..*(..))")
//	 public void logAll(JoinPoint point) throws Throwable {
//		 System.out.println("拦截方法");
//	 }
	
//	  @After("execution(* cn.telling.action..*(..))")
//	 public void after() {
//	 System.out.println("after");
//	 }

	// 方法执行的前后调用
	// @Around("execution(* com.wssys.controller.*(..))||execution(* com.bpm.*.web.account.*.*(..))")
	// @Around("execution(* com.wssys.controller.*(..))")
	// @Around("execution(* org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter.handle(..))")
	@Around("methodCachePointcut()")
	public Object around(ProceedingJoinPoint point) throws Throwable
	{
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getRequest();
		String ip = TCPIPUtil.getIpAddr(request);
		Principal user = (Principal) SecurityUtils.getSubject().getPrincipal();
		String loginName;

		if (user != null)
		{
			loginName = user.getLoginName();
			// name = user.name;
		} else
		{
			loginName = "匿名用户";
			// name = "匿名用户";
		}

		String monthRemark = getMthodRemark(point);
		String monthName = point.getSignature().getName();
		String packages = point.getThis().getClass().getName();
		if (packages.indexOf("$$EnhancerByCGLIB$$") > -1)
		{ // 如果是CGLIB动态生成的类
			try
			{
				packages = packages.substring(0, packages.indexOf("$$"));
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}

		Object[] method_param = null;

		Object object;
		try
		{
			method_param = point.getArgs(); // 获取方法参数
			// String param=(String) point.proceed(point.getArgs());
			object = point.proceed();
		} catch (Exception e)
		{
			logger.error(e.getMessage());
			throw e;
		}
		Syslog sysLog = new Syslog();
		sysLog.setIpAddress(ip);
		sysLog.setLoginName(loginName);
		sysLog.setMethodName(packages + "." + monthName);
		sysLog.setMethodRemark(monthRemark);
		// 这里有点纠结 就是不好判断第一个object元素的类型 只好通过 方法描述来 做一一 转型感觉 这里 有点麻烦 可能是我对 aop不太了解 希望懂的高手在回复评论里给予我指点
		// 有没有更好的办法来记录操作参数 因为参数会有 实体类 或者javabean这种参数怎么把它里面的数据都解析出来?
		if (StringHelperTools.nvl(monthRemark).equals("会员新增"))
		{
			// PusFrontUser pfu = (PusFrontUser) method_param[0];
			// sysLog.setOperatingContent("新增会员:" + pfu.getAccount());
		} else if (StringHelperTools.nvl(monthRemark).equals("新增角色"))
		{
			Role pr = (Role) method_param[0];
			sysLog.setOperatingContent("新增角色:" + pr.getName());
		} else if (StringHelperTools.nvl(monthRemark).equals("用户登录"))
		{
			User currUser = (User) method_param[0];
			sysLog.setOperatingContent("登录帐号:" + currUser.getUsername());
		} else if (StringHelperTools.nvl(monthRemark).equals("用户退出"))
		{
			sysLog.setOperatingContent("具体请查看用户登录日志");
		} else if (StringHelperTools.nvl(monthRemark).equals("角色名称修改"))
		{
			Role pr = (Role) method_param[0];
			sysLog.setOperatingContent("修改角色:" + pr.getName());
		} else if (StringHelperTools.nvl(monthRemark).equals("新增后台用户"))
		{
			User psu = (User) method_param[0];
			sysLog.setOperatingContent("新增后台用户:" + psu.getUsername());
		} else if (StringHelperTools.nvl(monthRemark).equals("更新菜单"))
		{
			Menu pm = (Menu) method_param[0];
			sysLog.setOperatingContent("更新菜单:" + pm.getName());
		} else if (StringHelperTools.nvl(monthRemark).equals("保存菜单"))
		{
			Menu pm = (Menu) method_param[0];
			sysLog.setOperatingContent("保存菜单:" + pm.getName());
		} else if (StringHelperTools.nvl(monthRemark).equals("修改公司"))
		{
			// ComPanyForm ciform = (ComPanyForm) method_param[0];
			// sysLog.setOperatingContent("修改公司:" + ciform.getName());
		} else if (StringHelperTools.nvl(monthRemark).equals("联系人更新"))
		{

		} else if (StringHelperTools.nvl(monthRemark).equals("修改货物"))
		{
			// GoodsForm goodsForm = (GoodsForm) method_param[0];
			// sysLog.setOperatingContent("修改货物（货物id/编号）:" + goodsForm.getId());
		} else if (StringHelperTools.nvl(monthRemark).equals("打印出库单"))
		{
			// DeliverBean owh= (DeliverBean) method_param[0];
			// sysLog.setOperatingContent("出库单单号:" + owh.getCknum());
		} else if (StringHelperTools.nvl(monthRemark).equals("打印提单"))
		{
			// BolBean bol= (BolBean) method_param[0];
			sysLog.setOperatingContent("打印提单");
		} else if (StringHelperTools.nvl(monthRemark).equals("系统左侧菜单查询"))
		{
			sysLog.setOperatingContent("无");
		} else if (StringHelperTools.nvl(monthRemark).equals("生成采购合同"))
		{
			sysLog.setOperatingContent("合同编号： " + method_param[1]);
		} else
		{
			if (method_param.length > 0)
			{
				sysLog.setOperatingContent("操作参数:" + method_param[0]);
			} else
			{
				sysLog.setOperatingContent("无操作参数");
			}

		}

		logService.saveLog(sysLog);
		return object;
	}

	// 方法运行出现异常时调用
	 @AfterThrowing(pointcut = "execution(* cn.telling.action..*(..))",
	 throwing = "ex")
	public void afterThrowing(Exception ex)
	{
	    logger.error("afterThrowing"+ex);
	}

	// 获取方法的中文备注____用于记录用户的操作日志描述
	public static String getMthodRemark(ProceedingJoinPoint joinPoint) throws Exception
	{
		String targetName = joinPoint.getTarget().getClass().getName();
		String methodName = joinPoint.getSignature().getName();
		Object[] arguments = joinPoint.getArgs();

		Class<?> targetClass = Class.forName(targetName);
		Method[] method = targetClass.getMethods();
		String methode = "";
		for (Method m : method)
		{
			if (m.getName().equals(methodName))
			{
				Class<?>[] tmpCs = m.getParameterTypes();
				if (tmpCs.length == arguments.length)
				{
					MethodLog methodCache = m.getAnnotation(MethodLog.class);
					if (methodCache != null)
					{
						methode = methodCache.remark();
					}
					break;
				}
			}
		}
		return methode;
	}

}
