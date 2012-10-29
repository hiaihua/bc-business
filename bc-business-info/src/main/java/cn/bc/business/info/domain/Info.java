/**
 * 
 */
package cn.bc.business.info.domain;

import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import cn.bc.identity.domain.RichFileEntityImpl;

/**
 * ��Ϣ
 * 
 * @author dragon
 */
@Entity
@Table(name = "BS_INFO")
public class Info extends RichFileEntityImpl {
	private static final long serialVersionUID = 1L;
	/** ״̬���ݸ� */
	public static final int STATUS_DRAFT = 0;
	/** ״̬���ѷ��� */
	public static final int STATUS_ISSUED = 1;
	/** ״̬���ѹ鵵 */
	public static final int STATUS_ARCHIVED = 2;

	/** ���ͣ���˾�ļ� */
	public static final int TYPE_COMPANYGILE = 0;
	/** ���ͣ������ļ� */
	public static final int TYPE_REGULATION = 1;
	/** ���ͣ�֪ͨ */
	public static final int TYPE_NOTICE = 2;

	private Calendar sendDate;// ����ʱ��
	private Calendar endDate;// ��������
	private String subject;// ��Ϣ����
	private String content;// ��ϸ����
	private int type;// ����
	private String source;// ��Ϣ��Դ���繫��ķ������ţ�������Ϣ�����ӵ�ַ�ȣ������ʽ����Ӧ����Ϣ���;���

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Column(name = "TYPE_")
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	@Column(name = "SOURCE_")
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	@Column(name = "SEND_DATE")
	public Calendar getSendDate() {
		return sendDate;
	}

	public void setSendDate(Calendar sendDate) {
		this.sendDate = sendDate;
	}

	@Column(name = "END_DATE")
	public Calendar getEndDate() {
		return endDate;
	}

	public void setEndDate(Calendar endDate) {
		this.endDate = endDate;
	}
}