//
//  ViewController.swift
//  WebView-Pilot
//
//  Created by Bernadette Theuretzbachner on 11.03.20.
//  Copyright © 2020 Bernadette Theuretzbachner. All rights reserved.
//

import UIKit
import WebKit
import AVFoundation

class ViewController: UIViewController {

    @IBOutlet weak var webView: WKWebView!
    @IBOutlet weak var activityIndicator: UIActivityIndicatorView!
    var urlString = ""
    
    override func viewDidLoad() {
        super.viewDidLoad()
        webView.navigationDelegate = self
        
        // simple url without JS
        guard let url = URL(string: self.urlString) else {
            return
        }
      
        // create request
        let request = URLRequest(url: url)
                
        // load request
        webView.load(request)
        
        // ask for camera permissions
        AVCaptureDevice.requestAccess(for: .video) { (granted) in
            if !granted {
                print("Camera permission denied")
            }
        }
    }
    
    override func present(_ viewControllerToPresent: UIViewController, animated flag: Bool, completion: (() -> Void)? = nil) {
      setUIDocumentMenuViewControllerSoureViewsIfNeeded(viewControllerToPresent)
      super.present(viewControllerToPresent, animated: flag, completion: completion)
    }

    func setUIDocumentMenuViewControllerSoureViewsIfNeeded(_ viewControllerToPresent: UIViewController) {
      if #available(iOS 13, *), viewControllerToPresent is UIDocumentMenuViewController && UIDevice.current.userInterfaceIdiom == .phone {
        // Prevent the app from crashing if the WKWebView decides to present a UIDocumentMenuViewController while it self is presented modally.
        viewControllerToPresent.popoverPresentationController?.sourceView = webView
        viewControllerToPresent.popoverPresentationController?.sourceRect = CGRect(x: webView.center.x, y: webView.center.y, width: 1, height: 1)
      }
    }
}

// handles WebView
extension ViewController: WKNavigationDelegate {
    
    // view has started loading
    func webView(_ webView: WKWebView, didStartProvisionalNavigation navigation: WKNavigation!) {
        activityIndicator.startAnimating()
        
        // put any javascript string in variable injectFunction (between the three """)
        let injectFunction = """
            window.__test = "placeholder javascript string"
            """
               
        // executes javascript
        self.webView?.evaluateJavaScript(injectFunction) { _, error in
            if let error = error {
                print("ERROR while evaluating javascript \(error)")
                }
            print("executed injected javascript")
            }
        }
    
        // view has finished loading
        func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
            // loading indicator
            activityIndicator.stopAnimating()
            activityIndicator.hidesWhenStopped = true
    }
    
    
    // workaround in case server has no certificate
    // which means page won't open because it's not safe
    func webView(_ webView: WKWebView, didReceive challenge: URLAuthenticationChallenge, completionHandler: @escaping (URLSession.AuthChallengeDisposition, URLCredential?) -> Void) {
        print("Allow all, even without credentials")
        guard let serverTrust = challenge.protectionSpace.serverTrust else {
            completionHandler(.cancelAuthenticationChallenge, nil)
            return
        }
        let exceptions = SecTrustCopyExceptions(serverTrust)
        SecTrustSetExceptions (serverTrust, exceptions)
        completionHandler(.useCredential, URLCredential(trust: serverTrust))
    }
}
