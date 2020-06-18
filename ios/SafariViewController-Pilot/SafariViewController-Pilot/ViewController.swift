//
//  ViewController.swift
//  SafariViewController-Pilot
//

import AVFoundation
import SafariServices
import UIKit

class ViewController: UIViewController {
    
    
    @IBOutlet weak var inputField: UITextField!
    
    @IBAction func loadButton(_ sender: Any) {
        checkCameraPermission() 
        let inputText: String = inputField.text!
        showSafariVC(inputText)
    }
    
    // present SFSafariViewController
    private func showSafariVC(_ stringURL: String) {
        guard let URL = URL(string: stringURL) else {
            return
        }
        
        let safariVC = SFSafariViewController(url: URL)
        present(safariVC, animated: true)
    }
    
    func safariViewControllerDidFinish(_ safariVC: SFSafariViewController) {
        safariVC.dismiss(animated: true, completion: nil)
    }
    
    // ask for camera permissions
    func checkCameraPermission() {
        AVCaptureDevice.requestAccess(for: .video) { (granted) in
            if !granted {
                print("Camera permission denied")
            }
        }
    }
}

