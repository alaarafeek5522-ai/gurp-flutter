import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const GurpApp());
}

class GurpApp extends StatelessWidget {
  const GurpApp({super.key});
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Gurp',
      debugShowCheckedModeBanner: false,
      theme: ThemeData.dark().copyWith(
        scaffoldBackgroundColor: const Color(0xFF0D1117),
      ),
      home: const GurpScreen(),
    );
  }
}

class GurpScreen extends StatefulWidget {
  const GurpScreen({super.key});
  @override
  State<GurpScreen> createState() => _GurpScreenState();
}

class _GurpScreenState extends State<GurpScreen> {
  static const _channel = MethodChannel('com.alaa.gurp/security');

  bool? _isRooted;
  bool? _isEmulator;
  bool? _isValidPackage;
  String? _signatureHash;
  String _decrypted = '';

  // رسالة Hello مشفرة بـ Base64
  static const _encryptedMsg = 'SGVsbG8sIEd1cnAhIPCfkZIgUHJvdGVjdGVkIGJ5IEFsYWE=';

  @override
  void initState() {
    super.initState();
    _runChecks();
  }

  Future<void> _runChecks() async {
    try {
      final isRooted = await _channel.invokeMethod<bool>('isRooted');
      final isEmulator = await _channel.invokeMethod<bool>('isEmulator');
      final isValidPackage = await _channel.invokeMethod<bool>('isValidPackage');
      final signatureHash = await _channel.invokeMethod<String>('getSignatureHash');
      final decrypted = await _channel.invokeMethod<String>(
        'decrypt', {'data': _encryptedMsg});

      setState(() {
        _isRooted = isRooted;
        _isEmulator = isEmulator;
        _isValidPackage = isValidPackage;
        _signatureHash = signatureHash;
        _decrypted = decrypted ?? '';
      });
    } catch (e) {
      setState(() => _decrypted = 'Error: $e');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              // الأيقونة
              ClipRRect(
                borderRadius: BorderRadius.circular(20),
                child: Image.asset('assets/images/icon.png',
                    width: 90, height: 90, fit: BoxFit.cover),
              ),
              const SizedBox(height: 20),

              // الرسالة المشفرة
              Text(
                _decrypted.isEmpty ? '...' : _decrypted,
                style: const TextStyle(
                    fontSize: 22, fontWeight: FontWeight.bold,
                    color: Colors.white),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 30),

              // نتائج الفحوصات
              _checkRow('Root Detection',
                  _isRooted == null ? '...' : (_isRooted! ? '⚠️ Rooted' : '✅ Clean')),
              _checkRow('Emulator Detection',
                  _isEmulator == null ? '...' : (_isEmulator! ? '⚠️ Emulator' : '✅ Real Device')),
              _checkRow('Package Integrity',
                  _isValidPackage == null ? '...' : (_isValidPackage! ? '✅ Valid' : '❌ Tampered')),

              const SizedBox(height: 20),

              // الـ Signature Hash
              if (_signatureHash != null)
                Container(
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: const Color(0xFF161B22),
                    borderRadius: BorderRadius.circular(10),
                    border: Border.all(color: Colors.green.withOpacity(0.3)),
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text('SHA-256 Signature:',
                          style: TextStyle(color: Colors.green,
                              fontSize: 11, fontWeight: FontWeight.bold)),
                      const SizedBox(height: 4),
                      SelectableText(
                        _signatureHash!,
                        style: const TextStyle(
                            color: Colors.white70, fontSize: 10,
                            fontFamily: 'monospace'),
                      ),
                    ],
                  ),
                ),

              const SizedBox(height: 24),
              const Text('🔒 Protected by Alaa',
                  style: TextStyle(color: Colors.white38, fontSize: 12)),
            ],
          ),
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _runChecks,
        backgroundColor: const Color(0xFF1565C0),
        child: const Icon(Icons.refresh_rounded),
      ),
    );
  }

  Widget _checkRow(String label, String value) => Padding(
    padding: const EdgeInsets.symmetric(vertical: 6),
    child: Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text(label, style: const TextStyle(color: Colors.white60, fontSize: 13)),
        Text(value, style: const TextStyle(color: Colors.white, fontSize: 13,
            fontWeight: FontWeight.bold)),
      ],
    ),
  );
}
