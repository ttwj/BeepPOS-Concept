BeepPOS-Concept
========

Test implementation of CEPAS 2.0 deduction on Android with a BLE NFC device 

Due to overwhelming demand to learn more about CEPAS 2.0, we have decided to open source this, do reach out if you encounter any issues!

Also, CEPAS 3.0 will be launching sometime in 2020-2021, as the new CEPAS 3.0 specifications are not publicly available at time of writing, this code will not work for new CEPAS 3.0 cards

DISCLAIMER
========

This repository is for research purposes only, the use of this code is your responsibility.

I take NO responsibility and/or liability for how you choose to use any of the source code available here. By using any of the files available in this repository, you understand that you are AGREEING TO USE AT YOUR OWN RISK. Once again, ALL files available here are for EDUCATION and/or RESEARCH purposes ONLY.

This project is not certified by EZ-Link Pte Ltd to perform transactions on its card. Do so at your own risk. I am not liable if you receive any complaints


I want to accept EZ-Link payments, what should I do?
=======
You should NOT be using this if you want to accept EZ-Link payments, this is purely for educational purposes to learn more about how CEPAS debit transactions work


If you wanted to accept EZ-Link Payments, it would be faster if you find an approved acquirer, and use ECR (Electronic Cash Register) via RS232 to connect your system to their pre-approved terminal (https://www.ezlink.com.sg/business-opportunities)
- NETS: Verifone/Ingenico Terminals (https://www.nets.com.sg/)
- Wirecard: PAX Terminals (https://www.wirecard.com)
- Mobile Eftpos Pte Ltd (https://www.eftpos.com.sg/)

These providers also accept more payment options (Credit Cards, NETS FlashPay, etc.), will be better for your business!


For this educational implementation, we used EZ-Link M-Payment (https://www.ezlink.com.sg/2017/10/ez-link-now-an-accepted-mode-of-payment-on-obike-mobile-application)
You should purchase the CEPAS specifications from SPRING Singapore, www.singaporestandardseshop.sg, to get a better understanding of the CEPAS transaction process as well.
