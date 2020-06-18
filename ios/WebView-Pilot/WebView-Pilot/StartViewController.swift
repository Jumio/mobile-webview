//
//  StartViewController.swift
//  WebView-Pilot
//

import UIKit

class StartViewController: UIViewController {
    @IBOutlet weak var inputField: UITextField!
    var inputText = ""
    
    override func viewDidLoad() {
       super.viewDidLoad()
    }
    
     // sends text input to webview
    @IBAction func done(_ sender: Any) {
        inputText = inputField.text!
        performSegue(withIdentifier: "urlSegue", sender: self)
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        let viewController = segue.destination as! ViewController
        viewController.urlString = inputText
    }
}
