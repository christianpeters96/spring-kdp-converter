var gulp = require("gulp");

gulp.task("default", function () {
  return gulp
    .src(["**", "*/**", "!package.json", "!package-lock.json", "!gulpfile.js"])
    .pipe(gulp.dest("../resources/static"));
});
