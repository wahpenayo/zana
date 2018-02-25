# wahpenayo at gmail dot com
# 2018-02-24
#-----------------------------------------------------------------
if (file.exists('e:/porta/projects/zana')) {
  setwd('e:/porta/projects/zana')
} else {
  setwd('c:/porta/projects/zana')
}
source('src/scripts/r/functions.r')
#-----------------------------------------------------------------
make.huber <- function (epsilon) {
  a <- 0.5 / epsilon
  b <- -0.5 * epsilon
  function (x) { ifelse((abs(x)<=epsilon),a*x*x,abs(x) + b) } }
#-----------------------------------------------------------------
make.huber <- function (epsilon) {
  function (x) {
    ifelse((abs(x)<=0.5*epsilon),
      x*x/epsilon,
      abs(x) - 0.25*epsilon) } }
#-----------------------------------------------------------------
dev.on(
  file=file.path('tst','huber'),
  aspect=0.5,
  width=1280)
h <- make.huber(1.0)
plot.function(h,from=-3,to=3,n=101)
dev.off()
#-----------------------------------------------------------------
make.huberqr <- function (epsilon,p) {
  q <- p-1
  lower <- -p * epsilon
  upper <- (1-p) * epsilon
  a0 <- (0.5*(1-p)) / (p*epsilon)
  a1 <- (0.5*p) / ((1-p)*epsilon)
  b <- -0.5 * p * (1-p) * epsilon
  function (x) { 
    ifelse((x<=lower),
      ((p-1)*x) + b,
      ifelse((x<=0.0),
        a0*x*x,
        ifelse((x<=upper),
          a1*x*x,
          (p*x) + b))) } }
#-----------------------------------------------------------------
dev.on(file=file.path('tst','huberqr'), aspect=0.5, width=1280)
h <- make.huberqr(1.0,0.25)
plot.function(h,from=-3,to=3,n=101)
dev.off()
#-----------------------------------------------------------------
