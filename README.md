# mobile-webview_pilot
Sample implementation for Jumio Netverify Web 4 within webviews on Android and iOS

### iOS: WebView vs. SafariViewController

#### WKWebView
Embeds web contet into the actual app, basically a view that loads and displays web content. WKWebKit allows to parse and render html, load and display images, and execute JavaScript. The is the better choice if web content needs to be customized, like doing JavaScript evaluation on the page.

#### SFSafariViewController
Provides a visible standard interface for browsing the web, which means responsibility of rendering web content is delegatd to Safari and web content is displayed in the actual browser. SafariViewController runs in a separate process from the host app, which can't “see” the URL or navigation happening inside it for safety reasons.
