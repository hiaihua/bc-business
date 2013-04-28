package cn.bc.spider;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * 基于HttpClient的基础 Callable
 * 
 * @author dragon
 * 
 */
public class HttpClientCallable<V> implements Callable<Result<V>> {
	protected static Log logger = LogFactory.getLog(HttpClientCallable.class);
	private String method;// 请求方法：get|post
	private String url;// 登录密码
	private String id;// 该次请求的标识
	private String encoding = "UTF-8";// 请求的编码
	private static ExpressionParser parser = new SpelExpressionParser();
	private String successExpression;// 用于判断请求是否成功的spel表达式：表达式上下文中含有document、html、this对象可以使用
	private String resultExpression;// 用于从文档中获取数据的spel表达式：表达式上下文中含有document、html、this对象可以使用
	private String userAgent;// 请求时使用的用户代理
	private Map<String, String> formData;// 表单参数
	private Map<String, String> httpParams;// http参数
	private boolean stream;// 响应的实体是否是流:如下载文件等为true

	protected HttpEntity entity;// 响应的实体信息
	protected Object content;// 响应的内容：文本或流
	protected Document document;// 请求的响应文本对应的jsop文档对象

	public HttpClientCallable() {
	}

	public Result<V> call() throws Exception {
		String url = getUrl();
		Map<String, String> kvs;

		// 创建请求
		HttpUriRequest request;
		if ("post".equalsIgnoreCase(method)) {
			HttpPost post = new HttpPost(url);
			// 设置表单参数
			kvs = getFormData();
			if (logger.isInfoEnabled())
				logger.info("formData=" + kvs);
			// System.out.println("formData=" + kvs);
			if (kvs != null && !kvs.isEmpty()) {
				List<NameValuePair> formData = new ArrayList<NameValuePair>();
				for (Entry<String, String> e : kvs.entrySet()) {
					formData.add(new BasicNameValuePair(e.getKey(), e
							.getValue()));
				}
				HttpEntity entity = new UrlEncodedFormEntity(formData,
						getEncoding());
				post.setEntity(entity);
			}
			request = post;
		} else {// 默认为get
			HttpGet get = new HttpGet(url);
			request = get;
		}

		// 设置http参数
		kvs = getHttpParams();
		if (logger.isInfoEnabled())
			logger.info("httpParams=" + kvs);
		// System.out.println("httpParams=" + kvs);
		if (kvs != null && !kvs.isEmpty()) {
			HttpParams httpParams = request.getParams();
			for (Entry<String, String> e : kvs.entrySet()) {
				httpParams.setParameter(e.getKey(), e.getValue());
			}
		}

		// 提交请求
		HttpResponse response = getHttpClient().execute(request);
		this.entity = response.getEntity();
		if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {// 请求成功
			// 解析响应的结果
			parseContent();

			// 返回结果
			return getResult();
		} else {// 请求失败
			Result<V> r = new Result<V>(false, null, this.isStream());
			// 记录失败的状态
			r.setContent(response.getStatusLine().getStatusCode() + ":"
					+ response.getStatusLine().getReasonPhrase());
			return r;
		}
	}

	/**
	 * 解析响应
	 * 
	 * @throws IOException
	 */
	protected void parseContent() throws Exception {
		if (this.isStream()) {
			parseStream(this.entity.getContent());
		} else {
			parseHtml();
		}
	}

	/**
	 * 解析响应为文件流
	 */
	protected void parseStream(InputStream stream) throws Exception {

	}

	/**
	 * 解析响应为文本
	 * 
	 * @throws IOException
	 */
	protected void parseHtml() throws IOException {
		this.content = EntityUtils.toString(this.entity);
		if (logger.isDebugEnabled()) {
			logger.debug("html=" + this.content);
		}
		System.out.println("html=" + this.content);

		// 解析DOM
		parseDocument();
	}

	public String getId() {
		return id;
	}

	public HttpClientCallable<V> setId(String id) {
		this.id = id;
		return this;
	}

	public boolean isStream() {
		return stream;
	}

	public void setStream(boolean stream) {
		this.stream = stream;
	}

	public String getUrl() {
		return url;
	}

	public HttpClientCallable<V> setUrl(String url) {
		this.url = url;
		return this;
	}

	public HttpClient getHttpClient() {
		HttpClient c;
		if (this.id == null) {
			c = HttpClientFactory.create();
		} else {
			c = HttpClientFactory.get(this.id);
		}
		if (this.userAgent != null) {
			c.getParams().setParameter(HttpMethodParams.USER_AGENT,
					this.userAgent);
		}
		return c;
	}

	public String getSuccessExpression() {
		return successExpression;
	}

	public void setSuccessExpression(String successExpression) {
		this.successExpression = successExpression;
	}

	public String getResultExpression() {
		return resultExpression;
	}

	public void setResultExpression(String resultExpression) {
		this.resultExpression = resultExpression;
	}

	public Object getContent() {
		return content;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	/**
	 * 解析请求响应返回的信息
	 * 
	 * @return
	 */
	public void parseDocument() {
		document = Jsoup.parse(this.content.toString());

		// 无缩进格式
		// document.outputSettings().prettyPrint(false);
	}

	/**
	 * 获取请求的编码
	 * 
	 * @return
	 */
	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public Document getDocument() {
		return document;
	}

	/**
	 * 获取POST请求需提交的表单参数
	 * 
	 * @return
	 */
	protected Map<String, String> getFormData() {
		if (this.formData == null)
			this.formData = new HashMap<String, String>();
		return this.formData;
	}

	/**
	 * 获取请求需提交的http参数
	 * 
	 * @return
	 */
	public Map<String, String> getHttpParams() {
		if (this.httpParams == null)
			this.httpParams = new HashMap<String, String>();
		return httpParams;
	}

	/**
	 * 添加一个POST提交参数
	 * 
	 * @param key
	 *            键
	 * @param value
	 *            值
	 */
	public void addFormData(String key, String value) {
		if (this.formData == null)
			this.formData = new HashMap<String, String>();
		this.formData.put(key, value);
	}

	/**
	 * 添加一堆POST提交参数
	 * 
	 * @param params
	 */
	public void addFormData(Map<String, String> params) {
		if (params == null)
			return;
		if (this.formData == null)
			this.formData = new HashMap<String, String>();
		this.formData.putAll(params);
	}

	/**
	 * 添加一个请求参数
	 * 
	 * @param key
	 *            键
	 * @param value
	 *            值
	 */
	public void addHttpParam(String key, String value) {
		if (this.httpParams == null)
			this.httpParams = new HashMap<String, String>();
		this.httpParams.put(key, value);
	}

	/**
	 * 添加一堆POST提交参数
	 * 
	 * @param params
	 */
	public void addHttpParam(Map<String, String> params) {
		if (params == null)
			return;
		if (this.httpParams == null)
			this.httpParams = new HashMap<String, String>();
		this.httpParams.putAll(params);
	}

	private <T> T getExpressionValue(String expression, Class<T> clazz) {
		Expression exp = parser.parseExpression(expression);
		try {
			return exp.getValue(this, clazz);
		} catch (EvaluationException e) {
			logger.warn(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * 获取返回值
	 * 
	 * @return 如果设置了resultExpression则返回此表达式计算的结果，否则返回isSuccess()用于判断请求是否成功
	 */
	public Result<V> getResult() {
		Boolean success = isSuccess();
		Result<V> r = new Result<V>(success, success ? parseData() : null,
				this.stream);
		r.setContent(content);
		return r;
	}

	/**
	 * 解析通过spel获取的数据
	 * 
	 * @return
	 */
	protected V parseData() {
		if (resultExpression != null) {
			return parseData(getExpressionValue(this.resultExpression,
					Object.class));
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	protected V parseData(Object data) {
		return (V) data;
	}

	/**
	 * 判断请求是否成功
	 * 
	 * @return
	 */
	public Boolean isSuccess() {
		if (this.successExpression != null)
			return getExpressionValue(this.successExpression, Boolean.class);
		else
			return true;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}
}
