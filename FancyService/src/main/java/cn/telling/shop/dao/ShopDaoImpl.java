package cn.telling.shop.dao;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import cn.telling.shop.vo.Article;
import cn.telling.shop.vo.ProductSub;
import cn.telling.shop.vo.ProductVo;
import cn.telling.utils.OracleAutoInjection;

/**   
 * @Title: ShopDaoImpl.java 
 * @Package cn.telling.shop.dao 
 * @Description: (描述该文件做什么) 
 * @author 操圣
 * @date 2016-1-18 下午3:22:49 
 * @version V1.0   
 */
@Repository
public class ShopDaoImpl implements IShopDao {
  
  @Resource(name="film-template")
  private JdbcTemplate jdbcTemplate;
  /* (non-Javadoc)
   * @see cn.telling.shop.dao.IShopDao#getShops()
   */
  @Override
  public List<Article> getShops() {
    //  Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see cn.telling.shop.dao.IShopDao#getTongxun()
   */
  @Override
  public List<ProductSub> getTongxun() {
    //  Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see cn.telling.shop.dao.IShopDao#getProvince()
   */
  @Override
  public List<ProductSub> getProvince() {
    //  Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see cn.telling.shop.dao.IShopDao#getProduct()
   */
  @Override
  public List<ProductVo> getProduct() {
    //  Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see cn.telling.shop.dao.IShopDao#getProSub(java.math.BigDecimal)
   */
  @Override
  public List<ProductSub> getProSub(BigDecimal pId) {
    //  Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see cn.telling.shop.dao.IShopDao#getHJHProSub(java.math.BigDecimal)
   */
  @Override
  public List<ProductSub> getHJHProSub(BigDecimal pId) {
    //  Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see cn.telling.shop.dao.IShopDao#getProSub()
   */
  @Override
  public List<ProductSub> getProSub() {
    //  Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see cn.telling.shop.dao.IShopDao#getHJHProSub()
   */
  @Override
  public List<ProductSub> getHJHProSub() {
    //  Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see cn.telling.shop.dao.IShopDao#getPLs()
   */
  @Override
  public List<ProductVo> getPLs() {
  String sql="select pm.productid,pm.productname,pm.saletype from product_main pm";
    return jdbcTemplate.query(sql, new RowMapper<ProductVo>()
        {

          @Override
          public ProductVo mapRow(ResultSet rs, int rowNum) throws SQLException {
            ProductVo pv=new ProductVo();
            OracleAutoInjection.Rs2Vo(rs, pv, null);
            return pv;
          }
      
        });

  }

}

