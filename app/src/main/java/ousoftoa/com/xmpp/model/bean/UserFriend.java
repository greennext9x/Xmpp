package ousoftoa.com.xmpp.model.bean;

import android.graphics.Bitmap;
import android.widget.ImageView;

import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import ousoftoa.com.xmpp.utils.ImageUtil;


/**
 * @author MZH
 *
 */
public class UserFriend {
	public String nickname;
	public String username;
	public String truename;
	public String email;
	public String headimg;
	public String intro;
	public String mobile;
	public String sex;
	public String adr;
	public VCard vCard;
	public Bitmap bitmap;
	public double lat = 0.0;
	public double lon = 0.0;

	public UserFriend() {
		super();
	}

	public UserFriend(VCard vCard){
		if (vCard!=null) {
			nickname = vCard.getField("nickName");
			email = vCard.getField("email");
			intro = vCard.getField("intro");//介绍
			sex = vCard.getField("sex");
			mobile = vCard.getField("mobile");
			adr = vCard.getField("adr");
			String latAndlon = vCard.getField("latAndlon");
			if (latAndlon!=null && !latAndlon.equals("")) {
				String[] latAndLons = latAndlon.split(",");
				lat = Double.valueOf(latAndLons[0]);
				lon = Double.valueOf(latAndLons[1]);
			}
			this.vCard = vCard;
			bitmap = ImageUtil.getBitmapFromBase64String(vCard.getField("avatar"));
		}
	}
	
	
	public void showHead(ImageView imageView) {
		if (bitmap!=null) {
			imageView.setImageBitmap(bitmap);
		}
	}
}
