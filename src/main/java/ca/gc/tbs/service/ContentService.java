package ca.gc.tbs.service;

import org.springframework.stereotype.Service;

@Service
public class ContentService {

	public ContentService() {
		//BadWords.loadConfigs();
	}

	public String cleanContent(String content) {
		System.out.println("Before clean:" + content);
		String newContent = this.cleanPostalCode(content);
		if (!newContent.contentEquals(content)) {
			content = newContent;
			System.out.println("Postal code cleaned" + content);
		}
		System.out.println("Checking phone number...");
		newContent = this.cleanPhoneNumber(content);
		if (!newContent.contentEquals(content)) {
			content = newContent;
			System.out.println("Phone number cleaned" + content);
		}
		newContent = this.cleanSIN(content);
		if (!newContent.contentEquals(content)) {
			content = newContent;
			System.out.println("SIN number cleaned" + content);
		}
		newContent = this.cleanEmailAddress(content);
		if (!newContent.contentEquals(content)) {
			content = newContent;
			System.out.println("Email Address cleaned" + content);
		}
		return content;
	}

	private String cleanPostalCode(String content) {
		return content.replaceAll("[A-Za-z]\\d[A-Za-z][ -]?\\d[A-Za-z]\\d", "### ###");
	}

	private String cleanSIN(String content) {
		return content.replaceAll("\\d{3}\\s?\\d{3}\\s?\\d{3}", "### ### ###");
	}

	private String cleanPhoneNumber(String content) {
		content = content.replaceAll("(\\+\\d{1,2}\\s?)?1?\\-?\\.?\\s?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}", "# ### ### ###");
		content = content.replaceAll("(?:(?:\\+?1\\s*(?:[.-]\\s*)?)?(?:\\(\\s*([2-9]1[02-9]|[2-9][02-8]1|[2-9][02-8][02-9])\\s*\\)|([2-9]1[02-9]|[2-9][02-8]1|[2-9][02-8][02-9]))\\s*(?:[.-]\\s*)?)?([2-9]1[02-9]|[2-9][02-9]1|[2-9][02-9]{2})\\s*(?:[.-]\\s*)?([0-9]{4})(?:\\s*(?:#|x\\.?|ext\\.?|extension)\\s*(\\d+))?", "# ### ### ###");
		return content;
	}

	private String cleanEmailAddress(String content) {
		return content.replaceAll("([a-zA-Z0-9_\\-\\.]+)@([a-zA-Z0-9_\\-\\.]+)\\.([a-zA-Z]{2,5})", "####@####.####");
	}

}
