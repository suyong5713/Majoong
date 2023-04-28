import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:majoong/common/layout/default_layout.dart';
import 'package:majoong/view/login_screen.dart';
import 'package:majoong/viewmodel/login_viewmodel.dart';
import '../common/const/colors.dart';

class SplashScreen extends ConsumerWidget {
  const SplashScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final autoLoginState = ref.watch(checkAutoLoginProvider);
    return Scaffold(
      backgroundColor: PRIMARY_COLOR,
      body: Container(
        decoration: const BoxDecoration(
          image: DecorationImage(
              image: AssetImage(
                'res/majoong_logo.jpg',
              ),
              fit: BoxFit.cover),
        ),
        child: autoLoginState.when(
            data: (data) {
              if (data) {
              } else {
                Future.delayed(Duration(seconds: 2), () {
                  Navigator.pushReplacement(context,
                      MaterialPageRoute(builder: (context) => LoginScreen()));
                });
              }
            },
            error: (e, stack) {},
            loading: () {}),
      ),
    );
  }
}