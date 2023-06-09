import 'dart:io';

import 'package:json_annotation/json_annotation.dart';
import 'package:retrofit/retrofit.dart';

class WriteReviewRequestDto {
  double lng;
  double lat;
  String address;
  int score;
  bool isBright;
  bool isCrowded;
  File? reviewImage;
  String content;

  WriteReviewRequestDto(this.lng, this.lat, this.address, this.score,
      this.isBright, this.isCrowded, this.reviewImage, this.content);
}
