import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:majoong/common/util/logger.dart';
import 'package:majoong/model/response/base_response.dart';
import 'package:majoong/view/main/main_screen.dart';
import 'package:majoong/view/login/login_screen.dart';
import 'package:majoong/viewmodel/login/check_auto_login_provider.dart';
import 'package:majoong/viewmodel/login/login_provider.dart';
import '../../common/const/colors.dart';

class SplashScreen extends ConsumerWidget {
  const SplashScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final checkAutoLoginState = ref.read(checkAutoLoginProvider);
    final autoLoginState = ref.watch(loginProvider);
    if (checkAutoLoginState == 1) {
      logger.d('자동로그인 맞음!');
      ref.read(loginProvider.notifier).login(null);
      if (autoLoginState is BaseResponse && autoLoginState.status == 200) {
        Future.delayed(Duration(seconds: 2), () {
          Navigator.pushReplacement(
              context, MaterialPageRoute(builder: (context) => MainScreen()));
        });
      }
    } else if(checkAutoLoginState == 0) {
      logger.d('자동로그인 아님!');
      Future.delayed(Duration(seconds: 2), () {
        Navigator.pushReplacement(context,
            MaterialPageRoute(builder: (context) => const LoginScreen()));
      });
    }

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
      ),
    );
  }
}
