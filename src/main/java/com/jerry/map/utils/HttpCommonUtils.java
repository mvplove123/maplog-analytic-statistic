package com.jerry.map.utils;

import com.google.common.collect.Lists;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;


public class HttpCommonUtils {

    
    /**
     * 通过url发起http请求，返回数据流
     * @param urlStr
     * @return
     * @throws java.io.IOException
     */
        /**

         * 发送HTTP_GET请求

         * @see 该方法会自动关闭连接,释放资源

         * @param requestURL    请求地址(含参数)

         * @param decodeCharset 解码字符集,解析响应数据时用之,其为null时默认采用UTF-8解码

         * @return 远程主机响应正文

         */

        public static String sendGetRequest(String reqURL, String decodeCharset){

            String responseContent = null; //响应内容

            CloseableHttpClient  httpClient = HttpClients.createDefault(); //创建默认的httpClient实例
            
            HttpGet httpGet = new HttpGet(reqURL);           //创建org.apache.http.client.methods.HttpGet

            CloseableHttpResponse response=null;

            try{
                response = httpClient.execute(httpGet);              //执行GET请求
                HttpEntity entity = response.getEntity();            //获取响应实体

                if(null != entity){

                    responseContent = EntityUtils.toString(entity, decodeCharset==null ? "UTF-8" : decodeCharset);
                    EntityUtils.consume(entity); //Consume response content

                }
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }finally{

                try {
                    response.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } //关闭连接,释放资源
            }

            return responseContent;
    }
    
        /**

         * 发送HTTP_POST请求

         * @see 该方法会自动关闭连接,释放资源

         * @see 该方法会自动对<code>params</code>中的[中文][|][ ]等特殊字符进行<code>URLEncoder.encode(string,encodeCharset)</code>

         * @param reqURL        请求地址

         * @param params        请求参数

         * @param encodeCharset 编码字符集,编码请求数据时用之,其为null时默认采用UTF-8解码

         * @param decodeCharset 解码字符集,解析响应数据时用之,其为null时默认采用UTF-8解码

         * @return 远程主机响应正文

         */

        public static String sendPostRequest(String reqURL, Map<String, String> params, String encodeCharset, String decodeCharset){

            String responseContent = null;

            CloseableHttpClient httpClient = HttpClients.createDefault(); //创建默认的httpClient实例

             

            HttpPost httpPost = new HttpPost(reqURL);

            List<NameValuePair> formParams = Lists.newArrayList(); //创建参数队列

            for(Map.Entry<String,String> entry : params.entrySet()){

                formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));

            }

            CloseableHttpResponse response = null;
            try{

                httpPost.setEntity(new UrlEncodedFormEntity(formParams, encodeCharset==null ? "UTF-8" : encodeCharset));

                response = httpClient.execute(httpPost);

                HttpEntity entity = response.getEntity();

                if (null != entity) {

                    responseContent = EntityUtils.toString(entity, decodeCharset==null ? "UTF-8" : decodeCharset);

                    EntityUtils.consume(entity);

                }

            }catch(Exception e){

            }finally{

                try {
                    response.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            return responseContent;

        }
    
    
}
