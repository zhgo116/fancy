package cn.telling.common.uitl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import freemarker.template.Template;
import freemarker.template.TemplateException;

/**   
 * @Title: FreeMarkerUtil.java 
 * @Package cn.telling.common.uitl 
 * @Description: (描述该文件做什么) 
 * @author 操圣
 * @date 2017年3月18日 下午2:37:11 
 * @version V1.0   
 */
/**
 * @author HuifengWang 静态化方法
 **/
public class FreeMarkerUtil {
	/**
	 * 
	 * 生成HTML静态页面的公公方法
	 * @param fmc 
	 * @param templateName 模板的名称
	 * @param request
	 * @param map 生成模板需要的数据
	 * @param filePath 相对于web容器的路径
	 * @param fileName 要生成的文件的名称，带扩展名
	 * @author HuifengWang
	 * 
	 */
	public static void createHtml(FreeMarkerConfig fmc, String templateName,
			HttpServletRequest request, Map<?, ?> map, String filePath,
			String fileName) {
		Writer out = null;
		try {
			Template template = fmc.getConfiguration()
					.getTemplate(templateName);
			String htmlPath = request.getSession().getServletContext()
					.getRealPath(filePath)
					+ "/" + fileName;
			File htmlFile = new File(htmlPath);
			if (!htmlFile.getParentFile().exists()) {
				htmlFile.getParentFile().mkdirs();
			}
			if (!htmlFile.exists()) {
				htmlFile.createNewFile();
			}
			out = new OutputStreamWriter(new FileOutputStream(htmlPath),"UTF-8");
			template.process(map, out);
			out.flush();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TemplateException e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
				out = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * @param request
	 * @param filePath  文件存放的路径
	 * @param fileName 文件的名称，需要扩展名
	 * @author HuifengWang
	 * @return
	 */
	public static Map<String,Object> htmlFileHasExist(HttpServletRequest request,String filePath,
			String fileName) {
		Map<String,Object> map = new HashMap<String,Object>();
		String htmlPath = request.getSession().getServletContext()
				.getRealPath(filePath)
				+ "/" + fileName;
		File htmlFile = new File(htmlPath);
		if(htmlFile.exists()){
			map.put("exist", true);
		}else{
			map.put("exist",false);
		}
		return map ;
	}
}
