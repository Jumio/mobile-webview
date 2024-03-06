# Jumio Native Mobile Webviews
This repository contains sample implementations for Jumio Netverify Web 4 within native webviews on Android and iOS.

### iOS: WebView vs. SafariViewController

#### WKWebView
Embeds web content into the actual app. This is basically a view that loads and displays web content. WKWebKit allows you to parse and render HTML, load and display images, and execute JavaScript. The is the better choice if web content needs to be customized, such as doing JavaScript evaluation on the page.

#### SFSafariViewController
Provides a visible standard interface for browsing the web, which means the responsibility for rendering web content is delegated to Safari, and web content is displayed in the actual browser. SafariViewController runs in a separate process from the host app, which can't “see” the URL or navigation happening inside it for safety reasons.


## Contact
If you have any questions regarding our implementation guide please contact Jumio Customer Service at support@jumio.com. The Jumio online helpdesk contains a wealth of information regarding our service including demo videos, product descriptions, FAQs and other things that may help to get you started with Jumio. [Check it out at here](https://support.jumio.com).

## Copyright
&copy; Jumio Corporation, 100 Mathilda Place Suite 100 Sunnyvale, CA 94086

The source code and software available on this website (“Software”) is provided by Jumio Corporation or its affiliated group companies (“Jumio”) “as is” and any express or implied warranties, including, but not limited to, the implied warranties of merchantability and fitness for a particular purpose are disclaimed. In no event shall Jumio be liable for any direct, indirect, incidental, special, exemplary, or consequential damages (including but not limited to procurement of substitute goods or services, loss of use, data, profits, or business interruption) however caused and on any theory of liability, whether in contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use of this Software, even if advised of the possibility of such damage. In any case, your use of this Software is subject to the terms and conditions that apply to your contractual relationship with Jumio. As regards Jumio’s privacy practices, please see our privacy notice available here: [Privacy Policy](https://www.jumio.com/privacy-center/privacy-notices/online-services-notice/).
