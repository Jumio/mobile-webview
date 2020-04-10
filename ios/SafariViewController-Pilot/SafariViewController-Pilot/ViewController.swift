//
//  ViewController.swift
//  SafariViewController-Pilot
//
//  Created by Bernadette Theuretzbachner on 10.04.20.
//  Copyright © 2020 Bernadette Theuretzbachner. All rights reserved.
//
import SafariServices
import UIKit

class ViewController: UIViewController {
    
    
    @IBOutlet weak var inputField: UITextField!
    
    @IBAction func loadButton(_ sender: Any) {
        let inputText: String = inputField.text!
        showSafariVC(inputText)
    }
    
    private func showSafariVC(_ stringURL: String) {
        guard let URL = URL(string: stringURL) else {
            return
        }

        // Present SFSafariViewController
        let safariVC = SFSafariViewController(url: URL)
        present(safariVC, animated: true)
    }
        
    func safariViewControllerDidFinish(_ safariVC: SFSafariViewController) {
        safariVC.dismiss(animated: true, completion: nil)
    }
}

