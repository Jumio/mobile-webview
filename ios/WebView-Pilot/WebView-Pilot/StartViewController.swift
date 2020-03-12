//
//  StartViewController.swift
//  WebView-Pilot
//
//  Created by Bernadette Theuretzbachner on 12.03.20.
//  Copyright © 2020 Bernadette Theuretzbachner. All rights reserved.
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
